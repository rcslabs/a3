package com.rcslabs.rcl.message;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipMessagingService;
import com.rcslabs.rcl.event.MessagingEvent;
import com.rcslabs.rcl.messaging.IMessage;
import com.rcslabs.rcl.messaging.entity.Message;
import com.rcslabs.rcl.messaging.event.IMessagingEvent;
import com.rcslabs.rcl.telephony.entity.CallParams;
import gov.nist.javax.sip.header.SIPHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.TimeoutEvent;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.Collection;

public class SipMessageRequestObject extends SipRequestObject {
	private static Logger log = LoggerFactory.getLogger(SipMessageRequestObject.class);

	private IMessage message;
	private JainSipMessagingService service;

	public SipMessageRequestObject(
			IMessage message, 
			JainSipConnection connection, 
			ICallManager callManager,
			JainSipMessagingService service) 
	{
		super(callManager);
		this.message = message;
		this.service = service;
		setConnection(connection);
	}

	public SipMessageRequestObject(ICallManager callManager) {
		super(callManager);
	}

	@Override
	public void process(RequestEvent event) {
		try {
			Request request = event.getRequest();
			ToHeader toHeader = (ToHeader)request.getHeader(SIPHeaderNames.TO);
			FromHeader fromHeader = (FromHeader)request.getHeader(SIPHeaderNames.FROM);
			String toPhoneNumber = getPhoneNumber(toHeader.getAddress());
			String fromPhoneNumber = getPhoneNumber(fromHeader.getAddress());
			MessageFactory messageFactory = callManager.getMessageFactory();
			
			JainSipConnection connection = null;
			Collection<JainSipConnection> connections = callManager.findConnectionsByPhone(toPhoneNumber);
			if (connections.size()==0) {
				log.warn("Could not find connection for a phone number {}, ignoring MESSAGE request.", toPhoneNumber);
				sendResponse(messageFactory.createResponse(Response.NOT_FOUND, request));
				return;
			} else if (connections.size()==1) {
				connection = connections.iterator().next();
			} else {
				// TODO: Process incoming message for all connections !
				log.warn("There are more than one connection for a phone number {}, ignoring MESSAGE request.", toPhoneNumber);
				sendResponse(messageFactory.createResponse(Response.NOT_FOUND, request));
				return;
			}
			
			sendResponse(event, Response.OK);
			
			CallParams callParams = new CallParams(fromPhoneNumber, toPhoneNumber);
			Message message = new Message(new String((byte[])request.getContent()), callParams);
			
			JainSipMessagingService messagingService = connection.getService(JainSipMessagingService.class);
			messagingService.fireEvent(
					new MessagingEvent(IMessagingEvent.Type.MESSAGE_RECEIVED, message)
			);
		}
		catch(ServiceNotEnabledException e) {
			throw new IllegalStateException(e);
		}
		catch(Exception e) {
			log.warn("Exception while processing MESSAGE request", e);
		}
	}

	@Override
	public ClientTransaction send() throws Exception {
		Request request = createRequest(getConnection(), Request.MESSAGE);
		
		// # https://tracker.tdvt.ru/issues/9736
		// RFC 3428:
		// MESSAGE requests do not initiate dialogs.  User Agents MUST NOT
		// insert Contact header fields into MESSAGE requests. 
		request.removeHeader(ContactHeader.NAME);
		
		Address toAddress = createToAddress(message.getParams());
		((ToHeader)request.getHeader(SIPHeaderNames.TO)).setAddress(toAddress);
		request.setRequestURI(toAddress.getURI());
		
		//request.removeHeader(SIPHeaderNames.CONTACT); //Contact header should be omited in MESSAGE method
		
		request.setContent(
				message.getText(), 
				callManager.getHeaderFactory().createContentTypeHeader("text", "plain")
		);
		
		return sendRequest(request);
	}

	@Override
	protected SipResponseObject doCreateResponseObject() {
		return new SipMessageResponseObject(callManager, service, getConnection(), message);
	}

	@Override
	public void doTimeout(TimeoutEvent event) {
		MessagingEvent msgEvent = new MessagingEvent(IMessagingEvent.Type.MESSAGE_SEND_FAILED);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorText("Timeout");
		msgEvent.setErrorInfo(errorInfo);
		service.fireEvent(msgEvent);
	}

}
