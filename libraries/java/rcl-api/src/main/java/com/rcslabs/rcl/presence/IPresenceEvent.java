package com.rcslabs.rcl.presence;

import java.lang.reflect.Method;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.event.IEvent;
import com.rcslabs.rcl.presence.entity.IContactPresence;

public interface IPresenceEvent extends IEvent {
	public enum Type implements IEventType {
		SUBSCRIBE(listenerMethod("onSubscribe")),
		PRESENCE_STATE_CHANGED(listenerMethod("onPresenceStateChanged")),
		PRESENCE_ERROR(listenerMethod("onPresenceError"));
		
		private static Method listenerMethod(String name) {
			try {
				return IPresenceListener.class.getMethod(name, IPresenceEvent.class);
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
			return "Presence";
		}
	}
	
	Type getEventType();

	IContactPresence getPresenceState();
	
	IConnection getConnection();
}
