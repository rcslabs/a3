package com.rcslabs.rcl.message;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.event.CallFailedEvent;
import com.rcslabs.rcl.event.CallStartingEvent;
import com.rcslabs.rcl.telephony.entity.RejectReason;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ICallEvent.Type;
import com.rcslabs.rcl.telephony.event.ICallStartingEvent.Stage;
import gov.nist.javax.sip.header.SIPHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.*;
import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SipInviteResponseObject extends SipResponseObject {
	private static Logger log = LoggerFactory.getLogger(SipInviteResponseObject.class);

	private final JainSipCall call;

	public SipInviteResponseObject(ICallManager callManager, JainSipCall call) {
		super(callManager);
		this.call = call;
		setConnection(call.getConnection());
	}

	@Override
	protected void doOk(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		
		log.info("Client {} successfully initiated a call", userPhone);
		call.setAccepted(true);
		
		byte[] content = (byte[])event.getResponse().getContent();
		try {
			sendAck(event);
			call.fireEvent(new CallEvent(ICallEvent.Type.CALL_STARTED));
		}
		catch(RclException e) {
			log.error("Failed to start media for call " + call.getSipId(), e);
			callManager.removeCallFromPool(call.getSipId());
			call.fireErrorEvent("Failed to start media", e);
		}
		catch(Exception e) {
			log.error("Internal error: " + content.toString(), e);
			callManager.removeCallFromPool(call.getSipId());
			call.fireErrorEvent("Internal error: " + content.toString(), e);
		}
	}
	
	@Override
	protected void doUnauthorized(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		
		log.info("Performing WWW authenticate for client {}", userPhone);
		call.fireEvent(new CallStartingEvent(Stage.AUTHENTICATING));

		try {
			ClientTransaction tx = sendWwwAuthenticateRequest(event);
			call.setInviteClientTransaction(tx);
		}
		catch(Exception e) {
			log.error("processInvite", e);
			callManager.removeCallFromPool(call.getSipId());
			call.fireErrorEvent("Error while performing WWW Authenticate for user " + userPhone, e);
		}
	}
	
	@Override
	protected void doProxyAuthenticationRequired(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		
		log.info("Performing Proxy authenticate for client {}", userPhone);
		call.fireEvent(new CallStartingEvent(Stage.AUTHENTICATING));
		
		try {
			ClientTransaction tx = sendProxyAuthenticateRequest(event);
			call.setInviteClientTransaction(tx);
		}
		catch(Exception e) {
			log.error("processInvite", e);
			callManager.removeCallFromPool(call.getSipId());
			call.fireErrorEvent("Error while performing Proxy Authenticate for user " + userPhone, e);
		}
	}
	
	@Override
	protected void doTrying(ResponseEvent event) {
		call.fireEvent(new CallStartingEvent(Stage.TRYING));
	}
	
	@Override
	protected void doRinging(ResponseEvent event) {
		call.fireEvent(new CallStartingEvent(Stage.RINGING));
	}
	
	@Override
	protected void doSessionProgress(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		log.info("Session progress for client {}", userPhone);
		
		byte[] content = (byte[])event.getResponse().getContent();
		try {			
			call.fireEvent(new CallStartingEvent(Stage.SESSION_PROGRESS));
		}
		catch(Exception e) {
			log.error("Internal error: " + content.toString(), e);
			callManager.removeCallFromPool(call.getSipId());
			call.fireErrorEvent("Internal error: " + content.toString(), e);
		}
	}
	
	@Override
	protected void doOther(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		int statusCode = event.getResponse().getStatusCode();
		
		log.warn("Got non-ok response for user {}: {}, {}", new Object[] { userPhone, statusCode, event.getResponse().getReasonPhrase() });

		callManager.removeCallFromPool(call.getSipId());
		CallEvent callEvent;
		
		if(statusCode == Response.BUSY_HERE) {
			callEvent = new CallFailedEvent(RejectReason.BUSY);
		}
		else if(statusCode == Response.TEMPORARILY_UNAVAILABLE) {
			callEvent = new CallFailedEvent(RejectReason.UNAVAILABLE);
		}
		else if(statusCode == Response.DECLINE) {
			callEvent = new CallFailedEvent(RejectReason.DECLINE);
		}
		else if(statusCode == Response.REQUEST_TERMINATED) {
			callEvent = new CallEvent(Type.CALL_FINISHED);
			call.fireEvent(callEvent);
			return;
		}
		else {
			callEvent = new CallFailedEvent(RejectReason.ERROR);
			ErrorInfo errorInfo = new ErrorInfo();
			errorInfo.setErrorCode(event.getResponse().getStatusCode());
			errorInfo.setErrorText(event.getResponse().getReasonPhrase());
			callEvent.setErrorInfo(errorInfo);
		}
		
		call.fireEvent(callEvent);
	}
	
	private void sendAck(ResponseEvent responseEvent) throws InvalidArgumentException, SipException {
		Dialog dialog = responseEvent.getDialog();
//		dialog.incrementLocalSequenceNumber(); does not work
		Request ack = dialog.createAck(1L);
		((CSeqHeader)ack.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(
				((CSeqHeader)responseEvent.getResponse().getHeader(SIPHeaderNames.CSEQ)).getSeqNumber()
		);
		ack.addHeader(responseEvent.getClientTransaction().getRequest().getHeader(SIPHeaderNames.CONTACT));
		
		log.debug("Sending ACK: {}", ack);
		dialog.sendAck(ack);
		dialog.setApplicationData(ack);
		call.setCurrentDialog(dialog);
	}

}
