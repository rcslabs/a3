package com.rcslabs.rcl.core;

/**
 * An interface for the class that holds some application-specific data
 * of an arbitrary type. May be used to keep the data between several
 * operation in one session.
 *
 */
public interface IApplicationDataHolder {

	/**
	 * Sets the application-specific data, that may be afterwards retrieved.
	 * @param key a key, by which the data will be retrieved
	 * @param data the data
	 */
	void setApplicationData(Object key, Object data);
	
	/**
	 * Retrieves the previously saved application data.
	 * @param key a key, by which the data was saved
	 * @return the data
	 */
	Object getApplicationData(Object key);
	
	/**
	 * Removes the application data.
	 * @param key a key, by which the data was saved
	 */
	void removeApplicationData(Object key);
}
