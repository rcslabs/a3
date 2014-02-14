package com.rcslabs.rcl.message;

import javax.sip.ResponseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.CallEvent;
import com.rcslabs.rcl.telephony.event.ICallEvent;

public class SipByeResponseObject extends SipResponseObject {
	private static Logger log = LoggerFactory.getLogger(SipByeResponseObject.class);

	private final JainSipCall call;

	public SipByeResponseObject(ICallManager callManager, JainSipCall call) {
		super(callManager);
		this.call = call;
		setConnection(call.getConnection());
	}

	@Override
	protected void doOk(ResponseEvent event) {
		callManager.removeCallFromPool(call.getSipId());
		call.fireEvent(new CallEvent(ICallEvent.Type.CALL_FINISHED));
	}
	
	@Override
	protected void doOther(ResponseEvent event) {
		String userPhone = getConnection().getParams().getPhoneNumber();
		
		log.warn("Got non-ok response for BYE request, user = {}, reason = {}", userPhone, event.getResponse().getReasonPhrase());
		callManager.removeCallFromPool(call.getSipId());
		call.fireErrorEvent(event.getResponse().getStatusCode(), event.getResponse().getReasonPhrase());
	}

}
