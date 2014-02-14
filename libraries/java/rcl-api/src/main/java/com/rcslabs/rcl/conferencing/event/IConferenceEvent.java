package com.rcslabs.rcl.conferencing.event;

import java.lang.reflect.Method;

import com.rcslabs.rcl.conferencing.IConference;
import com.rcslabs.rcl.conferencing.IConferenceListener;
import com.rcslabs.rcl.conferencing.IConferenceServiceListener;
import com.rcslabs.rcl.core.event.IEvent;

/**
 * A conference event.
 *
 */
public interface IConferenceEvent extends IEvent {
	
	public enum Type implements IEventType {
		CONFERENCE_ERROR(listenerMethod("onConferenceError", IConferenceEvent.class)),
		CONFERENCE_STARTING(listenerMethod("onConferenceStarting", IConferenceEvent.class)),
		CONFERENCE_STARTED(listenerMethod("onConferenceStarted", IConferenceEvent.class)),
		CONFERENCE_FINISHED(listenerMethod("onConferenceFinished", IConferenceEvent.class)),
		CONFERENCE_FAILED(listenerMethod("onConferenceFailed", IConferenceEvent.class)),
		MEMBER_JOINED(listenerMethod("onMemberJoined", IConferenceMemberEvent.class)),
		MEMBER_LEFT(listenerMethod("onMemberLeft", IConferenceMemberEvent.class)),
		KICKED(listenerMethod("onKicked", IConferenceEvent.class)),
		
		CONFERENCE_INVITATION(serviceListenerMethod("onConferenceInvitation", IConferenceEvent.class));
		
		private static Method listenerMethod(String name, Class<?> eventType) {
			try {
				return IConferenceListener.class.getMethod(name, eventType);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		
		private static Method serviceListenerMethod(String name, Class<?> eventType) {
			try {
				return IConferenceServiceListener.class.getMethod(name, eventType);
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
			return "Conference";
		}
	}

	/**
	 * Returns a conference that generated this event.
	 */
	IConference getConference();
	
	/**
	 * Returns this event's type.
	 */
	Type getEventType();
}
