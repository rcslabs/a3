package com.rcslabs.rcl.messaging.entity;

import java.util.UUID;

import com.rcslabs.rcl.messaging.IMessage;
import com.rcslabs.rcl.telephony.entity.ICallParams;

public class Message implements IMessage {
	private String text;
	private ICallParams destination;
	private String id = UUID.randomUUID().toString();
	private long timestamp = System.currentTimeMillis();
	
	public Message(String text, ICallParams destination) {
		this.text = text;
		this.destination = destination.clone();
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public ICallParams getParams() {
		return destination;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
