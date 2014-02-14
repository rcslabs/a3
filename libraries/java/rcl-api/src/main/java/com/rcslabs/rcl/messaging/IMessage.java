package com.rcslabs.rcl.messaging;

import com.rcslabs.rcl.telephony.entity.ICallParams;

/**
 * Represents a message, sent via {@link IMessagingService}
 *
 */
public interface IMessage {

	/**
	 * Returns a message text.
	 */
	String getText();

	/**
	 * Returns a message params (destination etc.).
	 */
	ICallParams getParams();
	
	/**
	 * Returns a unique identifier of this message.
	 */
	String getId();
	
	/**
	 * Returns a time, when this message was sent.
	 */
	long getTimestamp();
}
