package ru.rcslabs.webcall.server.api;

import com.rcslabs.rcl.core.IService;
import com.rcslabs.rcl.core.event.IEvent;

/**
 * Interface for interacting with client side.
 *
 */
public interface IClientConnection extends IService {
	String getClientIpAddress();
	
	void onEvent(IEvent event);
	
	void invoke(String method, Object...params);
}
