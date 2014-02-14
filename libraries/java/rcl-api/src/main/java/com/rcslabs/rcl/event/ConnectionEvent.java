package com.rcslabs.rcl.event;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.event.IConnectionEvent;

/**
 * Event from {@link IConnection}
 *
 */
public class ConnectionEvent extends Event implements IConnectionEvent {
	private Type type;
	
	public ConnectionEvent(Type type) {
		this.type = type;
	}
	
	public Type getEventType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("ConnectionEvent [type=%s, errorInfo=%s]",
				type, getErrorInfo());
	}

}
