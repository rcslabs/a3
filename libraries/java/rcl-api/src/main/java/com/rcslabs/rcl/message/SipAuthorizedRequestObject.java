package com.rcslabs.rcl.message;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.message.Request;

import com.rcslabs.rcl.ICallManager;

public class SipAuthorizedRequestObject extends SipRequestObject {

	private final Request request;
	private final SipResponseObject wrappedResponseObject;

	public SipAuthorizedRequestObject(
			ICallManager callManager, 
			Request request, 
			SipResponseObject wrappedResponseObject) 
	{
		super(callManager);
		this.request = request;
		this.wrappedResponseObject = wrappedResponseObject;
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		return new SipAuthorizedResponseObject(callManager, wrappedResponseObject);
	}

	@Override
	public void process(RequestEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClientTransaction send() throws Exception {
		return sendRequest(request);
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		wrappedResponseObject.getRequestObject().doTimeout(event);
	}
}
