package com.rcslabs.rcl.message;

import gov.nist.javax.sip.header.SIPHeaderNames;

import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.Request;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipGlobalParams;
import com.rcslabs.rcl.presence.entity.IContactPresence;

public class SipPublishRequestObject extends SipRequestObject {

	private final IContactPresence presence;

	public SipPublishRequestObject(
			ICallManager callManager, 
			JainSipConnection connection, 
			IContactPresence presence) 
	{
		super(callManager);
		setConnection(connection);
		this.presence = presence;
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		return new SipPublishResponseObject(callManager, getConnection());
	}

	@Override
	public void process(RequestEvent event) {
		replyMethodNotAllowed(event);
	}
	
	@Override
	public ClientTransaction send() throws Exception {
		JainSipConnection connection = getConnection();
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		JainSipGlobalParams globalParams = callManager.getGlobalParams();
		
		Request request = createRequest(connection, Request.PUBLISH);
//		((CSeqHeader)request.getHeader(SIPHeaderNames.CSEQ)).setSeqNumber(
//				getConnection().getNextCSeq()
//		);
		
		request.setRequestURI(
				callManager.getAddressFactory().createSipURI(
						connection.getParams().getPhoneNumber(), 
						getSipServerAddress(connection)
				)
		);
		((FromHeader)request.getHeader(SIPHeaderNames.FROM)).setTag(UUID.randomUUID().toString());
		
		request.addHeader(headerFactory.createEventHeader("presence"));
		request.addHeader(headerFactory.createExpiresHeader(globalParams.getPublishExpires()));
		
		request.setContent(
				callManager.getPresenceHelper().marshal(presence, getPresenceUri(connection)),
				headerFactory.createContentTypeHeader("application", "pidf+xml"));
		
		return sendRequest(request);
	}
	
	private String getPresenceUri(JainSipConnection connection) {
		return String.format(
				"sip:%s@%s", 
				connection.getParams().getPhoneNumber(),
				callManager.getGlobalParams().getSipServerAddress()
		);
	}
	
	@Override
	public void doTimeout(TimeoutEvent event) {}
}
