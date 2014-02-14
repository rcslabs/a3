package com.rcslabs.rcl.message;

import gov.nist.javax.sip.header.SIPHeaderNames;

import java.text.ParseException;
import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;

import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;

public class SipSubscribeRequestObject extends SipRequestObject {

	private final String eventType;
	private final int expires;
	private final URI requestUri;
	private final Dialog dialog;

	public SipSubscribeRequestObject(
			ICallManager callManager, 
			JainSipConnection connection,
			String phoneNumber,
			String eventType,
			Dialog dialog,
			int expires) 
	{
		super(callManager);
		setConnection(connection);
		try {
			this.requestUri = callManager.getAddressFactory().createSipURI(
					phoneNumber,
					getSipServerAddress(getConnection())
			);
			this.eventType = eventType;
			this.expires = expires;
			this.dialog = dialog;
		} 
		catch (ParseException e) {
			throw new RclException(e);
		}
	}

	@Override
	public SipResponseObject doCreateResponseObject() {
		return new SipSubscribeResponseObject(callManager, getConnection());
	}

	@Override
	public void process(RequestEvent event) {
		replyMethodNotAllowed(event);
	}

	@Override
	public ClientTransaction send() throws Exception {
		AddressFactory addressFactory = callManager.getAddressFactory();
		HeaderFactory headerFactory = callManager.getHeaderFactory();
		
		Request request;
		if (dialog==null) { 
			request = createRequest(getConnection(), Request.SUBSCRIBE);
		} else {
			request = dialog.createRequest(Request.SUBSCRIBE);
		}
		
		request.setRequestURI(requestUri);
		
		String toUserName = null;
		Address toAddress = addressFactory.createAddress(
				toUserName, 
				requestUri);
		((ToHeader)request.getHeader(SIPHeaderNames.TO)).setAddress(toAddress);
		((FromHeader)request.getHeader(SIPHeaderNames.FROM)).setTag(UUID.randomUUID().toString());
		
		request.addHeader(headerFactory.createExpiresHeader(expires));
		request.addHeader(headerFactory.createEventHeader(eventType));
		
		if("presence.winfo".equals(eventType)) {
			request.addHeader(headerFactory.createAcceptHeader("application", "watcherinfo+xml"));
		}
		
		ContactHeader contactHeader = (ContactHeader)request.getHeader(SIPHeaderNames.CONTACT);
		SipURI uri = (SipURI)contactHeader.getAddress().getURI();
		if (uri.getTransportParam()==null) {
			uri.setTransportParam("udp");
		}
		if (uri.getUser()==null) {
			uri.setUser(getConnection().getPhoneNumber());
		}
		
		return sendRequest(request);
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		JainSipConnection connection = getConnection();
		connection.getPresenceService().fireErrorEvent(
				0, 
				"Subscribe for presence timed out, connection: " + connection.getId()
		);
	}
}
