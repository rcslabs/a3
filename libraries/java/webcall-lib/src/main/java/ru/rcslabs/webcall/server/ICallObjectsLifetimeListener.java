package ru.rcslabs.webcall.server;

import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.telephony.ICall;
/**
 * Listener  
 * @author dzernov
 */
public interface ICallObjectsLifetimeListener {
	void onConnectionCreated(IConnection conn);
	void onConnectionDestroyed(IConnection conn, String uid, IClientConnection client);
	void onCallCreated(ICall call);
	void onCallDestroyed(ICall call);
	void onClientDisconnected(IClientConnection client);
}
