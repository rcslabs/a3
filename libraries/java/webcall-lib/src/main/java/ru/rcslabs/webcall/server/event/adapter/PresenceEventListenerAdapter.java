package ru.rcslabs.webcall.server.event.adapter;

import ru.rcslabs.webcall.server.AppDataConstants;
import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.presence.IPresenceEvent;
import com.rcslabs.rcl.presence.IPresenceListener;

public class PresenceEventListenerAdapter implements IPresenceListener {
	
	public static final PresenceEventListenerAdapter DEFAULT_INSTANCE = new PresenceEventListenerAdapter();
	
	private IClientConnection getCallbackProxy(IPresenceEvent event) {
		return (IClientConnection)event
				.getConnection()
				.getApplicationData(AppDataConstants.WEB_CONNECTION);
	}

	@Override
	public void onSubscribe(IPresenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onPresenceStateChanged(IPresenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onPresenceError(IPresenceEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

}
