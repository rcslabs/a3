package com.rcslabs.rcl.event;

import com.rcslabs.rcl.messaging.IMessage;
import com.rcslabs.rcl.messaging.event.IMessagingEvent;

public class MessagingEvent extends Event implements IMessagingEvent {
	
	private final Type type;
	private IMessage message;

	public MessagingEvent(IMessagingEvent.Type type) {
		this.type = type;
	}
	
	public MessagingEvent(IMessagingEvent.Type type, IMessage message) {
		this(type);
		this.message = message;
	}

	@Override
	public IEventType getEventType() {
		return type;
	}

	@Override
	public IMessage getMessage() {
		return message;
	}
	
	public void setMessage(IMessage message) {
		this.message = message;
	}

}
