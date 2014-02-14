package com.rcslabs.rcl.message;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import gov.nist.javax.sip.header.SIPHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.header.CallIdHeader;

public class SipAckRequestObject extends SipRequestObject {
	private static Logger log = LoggerFactory.getLogger(SipAckRequestObject.class);

	public SipAckRequestObject(ICallManager callManager) {
		super(callManager);
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		return null;
	}

	@Override
	public void process(RequestEvent event) {
		JainSipCall call = callManager.getCallFromPool(
			((CallIdHeader)event.getRequest().getHeader(SIPHeaderNames.CALL_ID)).getCallId()
		);
		
		if(call != null) {
			//existing call, got ACK for OK for INVITE
			log.debug("Call acknowledged. Id: {}", call.getSipId());
			event.getDialog().setApplicationData(event.getRequest());
			call.setCurrentDialog(event.getDialog());
			String fromPhoneNumber = call.getParams().getFrom();

            call.fireEvent(new CallEvent(ICallEvent.Type.CALL_STARTED));
		}
		else {
			log.warn("Got ACK request for non-existing call.");
		}
	}

	@Override
	public ClientTransaction send() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doTimeout(TimeoutEvent event) {}
}
