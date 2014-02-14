package com.rcslabs.rcl.core;

import com.rcslabs.rcl.exception.ServiceNotEnabledException;

/**
 * Interface for a class which provides a service of type {@link IService}.
 *
 */
public interface IServiceable {
	
	/**
	 * Returns a service, specified by a class parameter.
	 * @param cls a service class
	 * @return an instance of a service
	 * @throws ServiceNotEnabledException if a class does not provide such service
	 */
	<T extends IService> T getService(Class<T> cls) throws ServiceNotEnabledException;
	
	/**
	 * Checks wheter the service of specified class is enabled.
	 * @param cls a service class
	 * @return true if service is enabled, false otherwise
	 */
	<T extends IService> boolean isServiceEnabled(Class<T> cls);
}
