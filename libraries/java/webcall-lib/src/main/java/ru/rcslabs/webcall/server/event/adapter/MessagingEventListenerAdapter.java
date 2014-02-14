package ru.rcslabs.webcall.server.event.adapter;

import ru.rcslabs.webcall.server.AppDataConstants;
import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.messaging.IMessagingServiceListener;
import com.rcslabs.rcl.messaging.event.IMessagingEvent;

public final class MessagingEventListenerAdapter implements
		IMessagingServiceListener {

	public static final MessagingEventListenerAdapter DEFAULT_INSTANCE = new MessagingEventListenerAdapter();
	
	private IClientConnection getCallbackProxy(IMessagingEvent event) {
		return (IClientConnection)event.getConnection()
				.getApplicationData(AppDataConstants.WEB_CONNECTION);
	}
	
	@Override
	public void onMessageSent(IMessagingEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onMessageReceived(IMessagingEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onMessageSendFailed(IMessagingEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

}
