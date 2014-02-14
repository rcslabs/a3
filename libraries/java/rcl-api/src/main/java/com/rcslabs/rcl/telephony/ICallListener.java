package com.rcslabs.rcl.telephony;

import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ICallFailedEvent;
import com.rcslabs.rcl.telephony.event.ICallFinishNotificationEvent;
import com.rcslabs.rcl.telephony.event.ICallStartingEvent;
import com.rcslabs.rcl.telephony.event.ICallTransferEvent;

/**
 * {@link ICall} events. 
 *
 */
public interface ICallListener {
	
	/**
	 * Upcalled when a call initiation process passes some
	 * certain stage. May be upcalled several times (including 0).
	 * @param event call event
	 */
	void onCallStarting(ICallStartingEvent event);
	
	/**
	 * Upcalled when a call is successfully started.
	 * @param event call event
	 */
	void onCallStarted(ICallEvent event);
	
	/**
	 * Upcalled when a call is finished (locally or remotely).
	 * @param event call event
	 */
	void onCallFinished(ICallEvent event);
	
	/**
	 * Upcalled when a call initiation failed.
	 * @param event call event
	 */
	void onCallFailed(ICallFailedEvent event);
	
	/**
	 * Upcalled when a call is successfully transfered.
	 * @param event call event
	 */
	void onCallTransfered(ICallTransferEvent event);
	
	/**
	 * Upcalled when a call transfer failed.
	 * @param event call event
	 */
	void onTransferFailed(ICallTransferEvent event);
	
	/**
	 * Upcalled when an error occured. Use event.getErrorInfo() to
	 * identify an error.
	 * @param event call event
	 */
	void onCallError(ICallEvent event);
	
	/**
	 * Upcalled when the call is going to be finished after some time.
	 * @param event call event
	 */
	void onCallFinishNotification(ICallFinishNotificationEvent event);

}
