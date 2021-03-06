package com.rcslabs.rcl.core.event;

import java.lang.reflect.Method;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IConnectionListener;

/**
 * Event that is generated by a connection.
 *
 */
public interface IConnectionEvent extends IEvent {

	/**
	 * An event type.
	 */
	public enum Type implements IEventType {
		CONNECTING(listenerMethod("onConnecting")),
		CONNECTED(listenerMethod("onConnected")),
		CONNECTION_BROKEN(listenerMethod("onConnectionBroken")),
		CONNECTION_FAILED(listenerMethod("onConnectionFailed")),
		CONNECTION_ERROR(listenerMethod("onConnectionError"));
		
		private static Method listenerMethod(String name) {
			try {
				return IConnectionListener.class.getMethod(name, IConnectionEvent.class);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
				
		private final Method listenerMethod;

		private Type(Method listenerMethod) {
			this.listenerMethod = listenerMethod;
		}
		
		public Method getListenerMethod() {
			return listenerMethod;
		}
		
		@Override
		public String getCategory() {
			return "Connection";
		}
	}

	/**
	 * Returns a connection, that generated this event.
	 */
	IConnection getConnection();

	/**
	 * Returns this event's type.
	 */
	Type getEventType();
}
