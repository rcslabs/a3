package com.rcslabs.rcl.test;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

import com.rcslabs.rcl.conferencing.IConferenceListener;
import com.rcslabs.rcl.conferencing.IConferenceServiceListener;
import com.rcslabs.rcl.conferencing.event.IConferenceEvent;
import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.core.event.IEvent.IEventType;
import com.rcslabs.rcl.presence.IPresenceEvent;
import com.rcslabs.rcl.presence.IPresenceListener;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;
import com.rcslabs.util.CollectionUtils;

public class TestReflectionSanity {
	private class ListenerInfo {
		
		public Class<?> listenerClass;
		public Class<? extends IEventType> listenerEventTypeClass;
		
		public ListenerInfo(Class<?> listenerClass, Class<? extends IEventType> listenerEventTypeClass) {
			this.listenerClass = listenerClass;
			this.listenerEventTypeClass = listenerEventTypeClass;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testListenerMethods() {
		ListenerInfo linfoArray[] = {
				new ListenerInfo(IConnectionListener.class, IConnectionEvent.Type.class),
				new ListenerInfo(ICallListener.class, ICallEvent.Type.class),
				new ListenerInfo(ITelephonyServiceListener.class, ITelephonyEvent.Type.class),
				new ListenerInfo(IPresenceListener.class, IPresenceEvent.Type.class),
				new ListenerInfo(IConferenceListener.class, IConferenceEvent.Type.class),
				new ListenerInfo(IConferenceServiceListener.class, IConferenceEvent.Type.class)
		};
		
		for(ListenerInfo linfo : linfoArray) {
			for(final Method method : linfo.listenerClass.getMethods()) {
				Assert.assertNotNull(
						"Failed to find listener method " + method.getName() + " in event type " + linfo.listenerEventTypeClass,
						CollectionUtils.findSingle(
								EnumSet.allOf((Class)linfo.listenerEventTypeClass), 
								new CollectionUtils.Criteria<IEventType>() {
									@Override
									public boolean satisfies(IEventType element) {
										return element.getListenerMethod().equals(method);
									}
								}
						)
				);
			}
		}
	}

}
