package com.rcslabs.rcl.telephony;

import com.rcslabs.rcl.telephony.event.ITelephonyEvent;


/**
 * Listener for {@link ITelephonyService}
 *
 */
public interface ITelephonyServiceListener {
	
	/**
	 * Upcalled when a new incoming call request is received.
	 * @param call a new call
	 * @param event event
	 */
	void onIncomingCall(ICall call, ITelephonyEvent event);
	
	/**
	 * Upcalled when an existing incoming call request needs
	 * to be cancelled. 
	 * 
	 * It usually happens when onIncomingCall()
	 * is already being upcalled. The client should immediately
	 * stop all further processing of onIncomingCall() event.
	 * 
	 * @param call an existing incoming call
	 * @param event event
	 */
	void onCancel(ICall call, ITelephonyEvent event);
}
