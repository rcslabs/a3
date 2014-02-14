package ru.rcslabs.webcall.server.event.adapter;

import ru.rcslabs.webcall.server.AppDataConstants;
import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.event.IConnectionEvent;

public class ConnectionEventListenerAdapter implements IConnectionListener {

	private IClientConnection getCallbackProxy(IConnectionEvent event) {
		return (IClientConnection)event.getConnection().getApplicationData(AppDataConstants.WEB_CONNECTION);
	}
	
	@Override
	public void onConnecting(IConnectionEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConnected(IConnectionEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConnectionBroken(IConnectionEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConnectionFailed(IConnectionEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

	@Override
	public void onConnectionError(IConnectionEvent event) {
		getCallbackProxy(event).onEvent(event);
	}

}
