package com.rcslabs.rcl.core;

import com.rcslabs.util.CollectionUtils.Criteria;
import com.rcslabs.util.CollectionUtils.Operation;


/**
 * Creates new {@link IConnection} instances.
 *
 */
public interface IRclFactory {
	
	/**
	 * Creates and returns a new {@link IConnection} instance.
	 */
	IConnection newConnection();
	
	/**
	 * Closes all connections and frees all system resources.
	 * 
	 * This method needs to be called upon application termination.
	 */
	void dispose();

	/**
	 * Finds an existing connection in pool by ID.
	 * @param id an ID
	 * @return existing connection or null, if connection does not exist
	 */
	IConnection findConnection(String id);

	/**
	 * Runs the specified operation for each connection that meets a specified
	 * criteria.
	 * 
	 * @param criteria a criteria
	 * @param operation an operation to run
	 */
	void forEachConnection(Criteria<IConnection> criteria, Operation<IConnection> operation);
}
