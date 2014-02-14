package com.rcslabs.rcl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.core.event.IEvent;

public abstract class AbstractListenable<T, E extends IEvent> implements IListenable<T> {
	private static Logger log = LoggerFactory.getLogger(AbstractListenable.class);
	
	private Set<T> listeners = Collections.synchronizedSet(new HashSet<T>());

	@Override
	public void addListener(T listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(T listener) {
		listeners.remove(listener);
	}
	
	protected Collection<T> getListenersCopy() {
		return new HashSet<T>(listeners);
	}
	
	public void fireEvent(E event) {
		for(T listener : getListenersCopy()) {
			try {
				event.getEventType().getListenerMethod().invoke(listener, event);
			}
			catch(Exception e) {
				log.warn("Got exception from listener", e);
			}
		}
	}
	
	public boolean hasListeners() {
		return listeners.size() > 0;
	}

}
