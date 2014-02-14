package com.rcslabs.rcl.message;

import javax.sip.ResponseEvent;
import javax.sip.message.Response;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.event.ConnectionEvent;

public class SipAuthorizedResponseObject extends SipResponseObject {

	private final SipResponseObject wrappedResponseObject;

	public SipAuthorizedResponseObject(ICallManager callManager, SipResponseObject wrappedResponseObject) {
		super(callManager);
		this.wrappedResponseObject = wrappedResponseObject;
	}

	@Override
	public void doResponse(ResponseEvent event) {
		int statusCode = event.getResponse().getStatusCode();
		switch(statusCode) {
		case Response.UNAUTHORIZED:
		case Response.PROXY_AUTHENTICATION_REQUIRED:
			ConnectionEvent connectionEvent = new ConnectionEvent(IConnectionEvent.Type.CONNECTION_ERROR);
			ErrorInfo errorInfo = new ErrorInfo();
			errorInfo.setErrorCode(statusCode);
			errorInfo.setErrorText(event.getResponse().getReasonPhrase());
			connectionEvent.setErrorInfo(errorInfo);
			wrappedResponseObject.getConnection().fireEvent(connectionEvent);
			break;
			
		default:
			wrappedResponseObject.doResponse(event);
			break;
		}
	}
	
}
