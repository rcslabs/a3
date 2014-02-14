package com.rcslabs.rcl.core;

import com.rcslabs.rcl.core.entity.IConnectionParams;

/**
 * IConnection is a user's registration/presence on the call server.<br>
 * Use this interface to register/unregister on the call server and 
 * create new calls.
 *
 */
public interface IConnection 
extends 
IListenable<IConnectionListener>, 
IParametrizable<IConnectionParams>,
IApplicationDataHolder,
IServiceable {

	/**
	 * Connects to a call server with the specified connection parameters.
	 * @param params the connections parameters
	 */
	void open(IConnectionParams params);
	
	/**
	 * Closes the connection to a call server.
	 */
	void close();
	
	/**
	 * Returns a unique connection ID
	 */
	String getId();
	
	IAddressUri getUri();
}
