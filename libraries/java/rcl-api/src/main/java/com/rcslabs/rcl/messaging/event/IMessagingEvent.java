package com.rcslabs.rcl.messaging.event;

import java.lang.reflect.Method;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.event.IEvent;
import com.rcslabs.rcl.messaging.IMessage;
import com.rcslabs.rcl.messaging.IMessagingServiceListener;

/**
 * A messaging event.
 *
 */
public interface IMessagingEvent extends IEvent {
	
	public enum Type implements IEventType {
		MESSAGE_SENT(listenerMethod("onMessageSent")),
		MESSAGE_RECEIVED(listenerMethod("onMessageReceived")),
		MESSAGE_SEND_FAILED(listenerMethod("onMessageSendFailed"));
		
		private static Method listenerMethod(String name) {
			try {
				return IMessagingServiceListener.class.getMethod(name, IMessagingEvent.class);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private final Method listenerMethod;

		private Type(Method listenerMethod) {
			this.listenerMethod = listenerMethod;
		}

		@Override
		public Method getListenerMethod() {
			return listenerMethod;
		}

		@Override
		public String getCategory() {
			return "Messaging";
		}
	}

	/**
	 * Returns a message for this event.
	 */
	IMessage getMessage();
	
	/**
	 * Returns a connection, for which the event was received.
	 */
	IConnection getConnection();
}
