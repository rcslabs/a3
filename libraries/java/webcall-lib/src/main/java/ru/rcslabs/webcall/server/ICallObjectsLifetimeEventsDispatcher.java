package ru.rcslabs.webcall.server;

import ru.rcslabs.webcall.server.api.IClientConnection;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.telephony.ICall;

public interface ICallObjectsLifetimeEventsDispatcher extends IListenable<ICallObjectsLifetimeListener> {
	void fireConnectionCreated(IConnection conn);
	void fireConnectionDestroyed(IConnection conn, String uid, IClientConnection client);
	void fireCallCreated(ICall call);
	void fireCallDestroyed(ICall call);
	void fireCallClientDestroyed(IClientConnection client);
}
