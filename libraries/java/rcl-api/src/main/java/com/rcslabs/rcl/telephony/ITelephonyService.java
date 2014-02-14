package com.rcslabs.rcl.telephony;

import java.util.List;

import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.core.IService;

/**
 * Provides telephony services such as establishing a call or a conference.
 *
 */
public interface ITelephonyService extends IService, IListenable<ITelephonyServiceListener> {

	/**
	 * Creates a new call, that can be started then.
	 * @return a new call instance
	 */
	ICall newCall();
	
	/**
	 * Returns a list of established calls for this service
	 * instance.
	 */
	List<ICall> getCalls();

	/**
	 * Finds a call by specified id.
	 * @param id a call id
	 * @return an existing call or null if call is not present
	 */
	ICall findCall(String id);
}
