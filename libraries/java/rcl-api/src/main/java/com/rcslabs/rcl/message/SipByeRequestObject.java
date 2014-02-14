package com.rcslabs.rcl.message;

import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ICallEvent.Type;

public class SipByeRequestObject extends SipRequestObject {
	private static Logger log = LoggerFactory.getLogger(SipByeRequestObject.class);

	private final JainSipCall call;

	public SipByeRequestObject(ICallManager callManager, JainSipCall call) {
		super(callManager);
		this.call = call;
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		return new SipByeResponseObject(callManager, call);
	}

	@Override
	public void process(RequestEvent event) {
		
		sendResponse(event, Response.OK);
		
		call.setCurrentDialog(null);
		callManager.removeCallFromPool(call.getSipId()); //calls.remove(call.getId());
		call.fireEvent(new CallEvent(ICallEvent.Type.CALL_FINISHED));

	}

	@Override
	public ClientTransaction send() throws Exception {
		Dialog dialog = call.getCurrentDialog();
		if(dialog == null) {
			log.warn(
					"No current dialog, so, no call (not started or already finished). Call ID: {}. Connection ID: {}", 
					call.getSipId(),
					call.getConnection().getId()
			);
			return null;
		}
		
		Request currentAck = (Request)dialog.getApplicationData();
		if(currentAck == null) throw new IllegalStateException("Internal error: no current ACK");
		
		Request byeRequest = dialog.createRequest(Request.BYE);
		((CSeqHeader)byeRequest.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(
				dialog.getLocalSeqNumber() + 1
		);
		ContactHeader ackContactHeader = (ContactHeader)currentAck.getHeader(SIPHeaderNames.CONTACT);
		if(ackContactHeader != null) {
			byeRequest.addHeader(ackContactHeader);
		}
		
		ClientTransaction tx = sendRequest(byeRequest, dialog);
		call.setCurrentDialog(null);
		//callManager.removeCallFromPool(call.getId()); //do this when we get response or timeout
		
		return tx;
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		log.warn("Bye timed out, call id: {}", call.getSipId());
		
		callManager.removeCallFromPool(call.getSipId());
		call.fireEvent(new CallEvent(Type.CALL_FINISHED));
	}
}
