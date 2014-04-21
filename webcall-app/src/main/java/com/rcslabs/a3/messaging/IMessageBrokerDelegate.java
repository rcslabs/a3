package com.rcslabs.a3.messaging;

public interface IMessageBrokerDelegate {
	
	void onMessageReceived(String channel, IMessage message);

    void handleOnMessageException(IMessage message, Throwable e);
}