package com.rcslabs.rcl;

import java.util.Map;

import com.rcslabs.rcl.telephony.entity.ICustomCallParams;

/**
 * Custom call params that specifies custom SIP headers
 * during INVITE.
 *
 */
public interface ICustomHeaderParams extends ICustomCallParams {

	/**
	 * Returns custom headers in a map [header name, header value].
	 * 
	 */
	Map<String, Object> getCustomHeaders();
}
