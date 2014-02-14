package com.rcslabs.rcl.presence;

/**
 * Listener for {@link IPresenceService} events.
 *
 */
public interface IPresenceListener {

	/**
	 * Upcalled on successfull subscription.
	 */
	void onSubscribe(IPresenceEvent event);

	/**
	 * Upcalled when the contact's status has changed.
	 */
	void onPresenceStateChanged(IPresenceEvent event);

	/**
	 * Upcalled when an error occured.
	 */
	void onPresenceError(IPresenceEvent event);
	
}
