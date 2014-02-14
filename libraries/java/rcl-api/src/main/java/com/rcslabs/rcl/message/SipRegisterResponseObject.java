package com.rcslabs.rcl.message;

import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.ResponseEvent;
import javax.sip.header.ExpiresHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.event.ConnectionEvent;

public class SipRegisterResponseObject extends SipResponseObject {
	private static Logger log = LoggerFactory.getLogger(SipRegisterResponseObject.class);
	
	public SipRegisterResponseObject(ICallManager callManager, JainSipConnection connection) {
		super(callManager);
		setConnection(connection);
	}
	
	@Override
	protected void doOk(ResponseEvent event) {
		ExpiresHeader expiresHeader =
			(ExpiresHeader)event.getClientTransaction().getRequest().getHeader(SIPHeaderNames.EXPIRES);
		boolean isUnregister = expiresHeader != null && expiresHeader.getExpires() == 0;
		JainSipConnection connection = getConnection();
		String userPhone = connection.getParams().getPhoneNumber();
		
		if(isUnregister) {
			log.info("Client {} successfully deregistered", userPhone);
			connection.onSuccessfulDeregistration();
		}
		else {
			log.info("Client {} successfully registered", userPhone);
			connection.onSuccessfulRegistration(defineExpires(event.getResponse()));
			
			if(connection.getParams().isPresenceEnabled()) {
				try {
					log.debug("Client {}: initializing presence...", userPhone);
					callManager.initPresence(connection);
				}
				catch(Exception e) {
					log.error("Got exception while initializing presence", e);
					connection.fireErrorEvent("Failed to initialize presence", e);
				}
			}
		}
	}
	
	@Override
	protected void doTrying(ResponseEvent event) {
		//do nothing on Trying
	}
	
	protected void doOther(ResponseEvent event) {
		JainSipConnection connection = getConnection();
		String userPhone = connection.getParams().getPhoneNumber();
		log.warn("Got non-ok response for user {}: {}, {}", new Object[] { userPhone, event.getResponse().getStatusCode(), event.getResponse().getReasonPhrase() });
		callManager.removeConnectionFromPool(connection); //in case of unsuccessfull UNREGISTER
		ConnectionEvent connectionEvent = new ConnectionEvent(IConnectionEvent.Type.CONNECTION_FAILED);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorCode(event.getResponse().getStatusCode());
		errorInfo.setErrorText(event.getResponse().getReasonPhrase());
		connectionEvent.setErrorInfo(errorInfo);
		getConnection().fireEvent(connectionEvent);
	}

}
