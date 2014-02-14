package com.rcslabs.rcl.message;

import javax.sip.ResponseEvent;

import com.rcslabs.rcl.core.entity.ErrorInfo;
import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipConnection;
import com.rcslabs.rcl.JainSipMessagingService;
import com.rcslabs.rcl.event.MessagingEvent;
import com.rcslabs.rcl.messaging.IMessage;
import com.rcslabs.rcl.messaging.event.IMessagingEvent;

public class SipMessageResponseObject extends SipResponseObject {

	private final IMessage message;
	private final JainSipMessagingService service;

	public SipMessageResponseObject(
			ICallManager callManager, 
			JainSipMessagingService service,
			JainSipConnection connection,
			IMessage message) 
	{
		super(callManager);
		this.service = service;
		this.message = message;
		setConnection(connection);
	}
	
	@Override
	protected void doAccepted(ResponseEvent event) {
		doOk(event);
	}
	
	@Override
	protected void doOk(ResponseEvent event) {
		service.fireEvent(new MessagingEvent(IMessagingEvent.Type.MESSAGE_SENT, message));
	}

	@Override
	protected void doOther(ResponseEvent event) {
		//log.warn("Got non-ok response for connection {}: {}, {}", new Object[] { getConnection().getId(), event.getResponse().getStatusCode(), event.getResponse().getReasonPhrase() });
		MessagingEvent messagingEvent = new MessagingEvent(IMessagingEvent.Type.MESSAGE_SEND_FAILED, message);
		ErrorInfo errorInfo = new ErrorInfo();
		errorInfo.setErrorCode(event.getResponse().getStatusCode());
		errorInfo.setErrorText(event.getResponse().getReasonPhrase());
		messagingEvent.setErrorInfo(errorInfo);
		service.fireEvent(messagingEvent);
	}

}
