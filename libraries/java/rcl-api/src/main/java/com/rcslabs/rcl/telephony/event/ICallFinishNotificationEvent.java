package com.rcslabs.rcl.telephony.event;

/**
 * This event notifies that the call is about to finish after
 * some time.
 *
 */
public interface ICallFinishNotificationEvent extends ICallEvent {

	/**
	 * Returns the time in seconds, after which the call is going
	 * to finish.
	 */
	long getTimeBeforeFinish();
}
