package ru.rcslabs.webcall.server;

import ru.rcslabs.webcall.server.api.IClientConnection;
import ru.rcslabs.webcall.server.app.IWebcallApplicationAware;
import ru.rcslabs.webcall.server.app.IWebcallApplication;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.exception.RclException;
import com.rcslabs.rcl.telephony.ICall;

public class WebcallApplicationService implements IWebcallApplicationAware, ICallObjectsLifetimeListener {

	private IWebcallApplication application;

	public IWebcallApplication getApplication() {
		return application;
	}

	public void setApplication(IWebcallApplication context) {
		this.application = context;
	}

	/*
	 * -------- ICallObjectsLifetimeListener implementation ---------
	 */
	public void onConnectionCreated(IConnection conn) {
	}
	public void onConnectionDestroyed(IConnection conn, String uid, IClientConnection client) {
	}
	public void onCallCreated(ICall call) {
	}
	public void onCallDestroyed(ICall call) {
	}
	public void onClientDisconnected(IClientConnection client) {
	}
	
	protected IConnection getConnection(String uid) {
		IConnection connection = getApplication().getRclFactory().findConnection(uid);
		if(connection == null) throw new RclException("No connection for UID " + uid);
		return connection;
	}
	
	protected IClientConnection getClientConnection(String uid) {
		return (IClientConnection)getConnection(uid).getApplicationData(AppDataConstants.WEB_CONNECTION);
	}

}