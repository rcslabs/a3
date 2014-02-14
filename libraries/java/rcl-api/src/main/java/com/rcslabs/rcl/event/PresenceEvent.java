package com.rcslabs.rcl.event;

import com.rcslabs.rcl.presence.IPresenceEvent;
import com.rcslabs.rcl.presence.entity.IContactPresence;

public class PresenceEvent extends Event implements IPresenceEvent {
	
	private final Type type;
	private IContactPresence presenceState;

	public PresenceEvent(Type type, IContactPresence presenceState) {
		this.type = type;
		this.presenceState = presenceState;
	}

	public PresenceEvent(Type type) {
		this.type = type;
	}

	@Override
	public Type getEventType() {
		return type;
	}

	@Override
	public IContactPresence getPresenceState() {
		return presenceState;
	}

}
