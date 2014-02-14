package com.rcslabs.rcl.message;

import gov.nist.javax.sip.header.Allow;

import java.util.Arrays;
import java.util.Collection;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.header.Header;
import javax.sip.message.Request;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.core.event.IConnectionEvent.Type;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.event.ConnectionEvent;

public class SipRegisterRequestObject extends SipRequestObject {

	private final int expires;

	public SipRegisterRequestObject(
			ICallManager callManager, 
			JainSipConnection connection,
			int expires) {
		super(callManager);
		this.expires = expires;
		setConnection(connection);
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		SipRegisterResponseObject ret = new SipRegisterResponseObject(callManager, getConnection());
		return ret;
	}

	@Override
	public void process(RequestEvent event) {
		replyMethodNotAllowed(event);
	}

	@Override
	public ClientTransaction send() throws Exception {
		Request registerReq = createRequest(getConnection(), Request.REGISTER);
		registerReq.addHeader(
				callManager.getHeaderFactory().createExpiresHeader(
						expires
				)
		);
		Header authHeader = (Header)getConnection().getApplicationData(ICallManager.APPDATA_AUTH_HEADER);
		if(authHeader != null) {
			registerReq.addHeader(authHeader);
		}

		return sendRequest(registerReq);
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		callManager.removeConnectionFromPool(getConnection());
		
		ConnectionEvent connectionEvent = new ConnectionEvent(Type.CONNECTION_FAILED);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorText("Timeout");
		connectionEvent.setErrorInfo(errorInfo);
		getConnection().fireEvent(connectionEvent);
	}
	
	@Override
	protected Collection<Allow> getAllowedMethods() {
		return Arrays.asList(
				new Allow(Request.INVITE),
				new Allow(Request.ACK),
				new Allow(Request.BYE),
				new Allow(Request.CANCEL),
				new Allow(Request.NOTIFY),
				new Allow(Request.MESSAGE)
		);
	}
}
