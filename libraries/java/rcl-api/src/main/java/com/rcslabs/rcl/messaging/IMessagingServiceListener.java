package com.rcslabs.rcl.messaging;

import com.rcslabs.rcl.messaging.event.IMessagingEvent;

/**
 * Listener for {@link IMessagingService}
 * @author vic
 *
 */
public interface IMessagingServiceListener {
	
	/**
	 * Upcalled when the message was successfully sent.
	 * 
	 * That means that the remote program received the message
	 * or accepted it for further processing.
	 * 
	 * @param event an event
	 */
	void onMessageSent(IMessagingEvent event);

	/**
	 * Upcalled when a message is received.
	 * @param event an event
	 */
	void onMessageReceived(IMessagingEvent event);
	
	/**
	 * Upcalled when a message send failed
	 * 
	 * @param event an event
	 */
	void onMessageSendFailed(IMessagingEvent event);
}
