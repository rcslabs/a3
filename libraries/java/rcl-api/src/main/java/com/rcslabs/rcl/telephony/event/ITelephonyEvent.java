package com.rcslabs.rcl.telephony.event;

import java.lang.reflect.Method;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.event.IEvent;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;

/**
 * A {@link ITelephonyService} event.
 *
 */
public interface ITelephonyEvent extends IEvent {
	public enum Type implements IEventType {
		INCOMING_CALL(listenerMethod("onIncomingCall")),
		CANCEL(listenerMethod("onCancel"));
		
		private static Method listenerMethod(String name) {
			try {
				return ITelephonyServiceListener.class.getMethod(
						name, 
						ICall.class, 
						ITelephonyEvent.class
				);
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
			return "Call";
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
	
	/**
	 * Return a call, that generated this event.
	 * @return a call or null, if there is no call for this event.
	 */
	ICall getCall();
}
