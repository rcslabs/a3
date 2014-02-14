package com.rcslabs.rcl.message;

import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.TelephonyEvent;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;
import gov.nist.javax.sip.header.SIPHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SipCancelRequestObject extends SipRequestObject {
	private static Logger log = LoggerFactory.getLogger(SipCancelRequestObject.class);

	private final JainSipCall call;

	public SipCancelRequestObject(ICallManager callManager, JainSipCall call) {
		super(callManager);
		this.call = call;
		setConnection(call.getConnection());
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		return new SipCancelResponseObject(callManager);
	}

	@Override
	public void process(RequestEvent event) {
		if(call != null) {
			log.debug("Call cancelled. Id: {}", call.getSipId());
			callManager.removeCallFromPool(call.getSipId());
			
			sendResponse(event, Response.OK);
			
			//terminate the INVITE transaction
			try {
				Response response = callManager.getMessageFactory().createResponse(
						Response.REQUEST_TERMINATED, 
						call.getInviteRequestObject().getRequest()
				);
				log.debug("sendResponse: {}", response);
				call.getInviteServerTransaction().sendResponse(response);
			} catch (Exception e) {
				log.error("Failed to terminate INVITE transaction, call id: " + call.getSipId(), e);
			}
			
			call.getConnection().getTelephonyService()
			.fireEvent(call, new TelephonyEvent(ITelephonyEvent.Type.CANCEL));
		}
		else {
			log.warn("Got CANCEL request for non-existing call.");
		}
	}

	@Override
	public ClientTransaction send() throws Exception {
		ClientTransaction tx = call.getInviteClientTransaction();
		if(tx == null) {
			throw new RclException("Call " + call.getSipId() + ": cannot finish the call that is not starting (CANCEL without INVITE)");
		}
		Request cancelRequest = tx.createCancel();
		cancelRequest.addHeader(tx.getRequest().getHeader(SIPHeaderNames.CONTACT));
		log.debug("Sending request: {}", cancelRequest);
		callManager.getProvider().sendRequest(cancelRequest);
		
		callManager.removeCallFromPool(call.getSipId());
		
		return tx;
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		log.warn("Cancel timed out, call id: {}", call.getSipId());
	}
}
