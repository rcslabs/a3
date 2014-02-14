package com.rcslabs.rcl.messaging;

import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.core.IService;

/**
 * Provides a service for sending instant messages.
 *
 */
public interface IMessagingService extends IService, IListenable<IMessagingServiceListener> {

	/**
	 * Sends a message.
	 */
	void send(IMessage message);
}
