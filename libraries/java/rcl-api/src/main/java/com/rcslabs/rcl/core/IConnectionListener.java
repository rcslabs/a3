package com.rcslabs.rcl.core;

import com.rcslabs.rcl.core.event.IConnectionEvent;

/**
 * {@link IConnection} events.
 *
 */
public interface IConnectionListener {
	
	/**
	 * Upcalled when the process of connecting to a call server
	 * passes some certain stage. May be upcalled several times
	 * (including 0).
	 * @param event connection event
	 */
	void onConnecting(IConnectionEvent event);
	
	/**
	 * Upcalled when the connection to a call server is established.
	 * @param event connection event
	 */
	void onConnected(IConnectionEvent event);
	
	/**
	 * Upcalled when the connection to a call server is broken (locally
	 * or remotely).
	 * @param event connection event
	 */
	void onConnectionBroken(IConnectionEvent event);
	
	/**
	 * Upcalled when the connection to a call server failed.
	 * @param event connection event
	 */
	void onConnectionFailed(IConnectionEvent event);

	/**
	 * Upcalled when an error occured. Use event.getErrorInfo() to
	 * identify an error.
	 * @param event connection event
	 */
	void onConnectionError(IConnectionEvent event);
}
