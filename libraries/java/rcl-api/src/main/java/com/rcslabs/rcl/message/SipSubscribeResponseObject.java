package com.rcslabs.rcl.message;

import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.ResponseEvent;
import javax.sip.header.EventHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipPresenceService;
import com.rcslabs.rcl.event.PresenceEvent;
import com.rcslabs.rcl.presence.IPresenceEvent;

public class SipSubscribeResponseObject extends SipResponseObject {
	private static Logger log = LoggerFactory.getLogger(SipSubscribeResponseObject.class);

	public SipSubscribeResponseObject(ICallManager callManager, JainSipConnection connection) {
		super(callManager);
		setConnection(connection);
	}

	@Override
	protected void doOk(ResponseEvent event) {
		Response response = event.getResponse();
		String phoneNumber = getPhoneNumber(((ToHeader)response.getHeader(SIPHeaderNames.TO)).getAddress());
		int expires = response.getExpires().getExpires();
		String eventType = ((EventHeader)event.getClientTransaction().getRequest().getHeader(SIPHeaderNames.EVENT)).getEventType();
		getConnection().getPresenceService().setSubscriptionInfo(
				phoneNumber, 
				expires, event.getDialog(), eventType);
		getConnection().getPresenceService().fireEvent(
				new PresenceEvent(IPresenceEvent.Type.SUBSCRIBE, null)
		);
	}
	
	@Override
	protected void doAccepted(ResponseEvent event) {
		doOk(event);
	}
	
	@Override
	protected void doOther(ResponseEvent event) {
		log.warn("Got non-ok response for SUBSCRIBE request, connection = {}, reason = {}", getConnection().getId(), event.getResponse().getReasonPhrase());
		
		JainSipPresenceService presenceService = getConnection().getPresenceService();
		String toPhoneNumber = getPhoneNumber(
				((ToHeader)event.getResponse().getHeader(SIPHeaderNames.TO)).getAddress()
		);
		presenceService.removeSubscription(toPhoneNumber);
		presenceService.fireErrorEvent(
				event.getResponse().getStatusCode(), 
				event.getResponse().getReasonPhrase());
	}

}
