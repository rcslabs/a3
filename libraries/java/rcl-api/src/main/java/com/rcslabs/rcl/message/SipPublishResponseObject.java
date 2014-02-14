package com.rcslabs.rcl.message;

import javax.sip.ResponseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;

public class SipPublishResponseObject extends SipResponseObject {
	private static Logger log = LoggerFactory.getLogger(SipPublishResponseObject.class);

	public SipPublishResponseObject(ICallManager callManager, JainSipConnection connection) {
		super(callManager);
		setConnection(connection);
	}
	
	/**
	 * Handler 200 OK response.
	 * 
	 * @param event a response event
	 */
	protected void doOk(ResponseEvent event) {
		connection.getPresenceService().onSuccessfulPublish(defineExpires(event.getResponse()));
	}

	
	@Override
	protected void doOther(ResponseEvent event) {
		log.warn("Got non-ok response for PUBLISH request, connection = {}, reason = {}", getConnection().getId(), event.getResponse().getReasonPhrase());
		getConnection().getPresenceService().fireErrorEvent(
				event.getResponse().getStatusCode(), 
				event.getResponse().getReasonPhrase());
	}

}
