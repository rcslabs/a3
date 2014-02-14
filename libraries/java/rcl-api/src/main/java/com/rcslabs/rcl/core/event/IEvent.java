package com.rcslabs.rcl.core.event;

import java.lang.reflect.Method;

import com.rcslabs.rcl.core.entity.ErrorInfo;

/**
 * An event.
 *
 */
public interface IEvent {
	
	public interface IEventType {
		Method getListenerMethod();
		String getCategory();
	}
	
	/**
	 * Returns this event's type.
	 */
	IEventType getEventType();
	
	/**
	 * Returns an error information if this is an error event.
	 * @return an error info or null, if there is no error info.
	 */
	ErrorInfo getErrorInfo();
}
