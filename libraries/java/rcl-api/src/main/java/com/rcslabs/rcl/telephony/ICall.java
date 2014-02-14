package com.rcslabs.rcl.telephony;

import com.rcslabs.rcl.core.IApplicationDataHolder;
import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IListenable;
import com.rcslabs.rcl.core.IParametrizable;
import com.rcslabs.rcl.core.event.IConnectionEvent;
import com.rcslabs.rcl.telephony.entity.CallType;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.entity.MediaParams;
import com.rcslabs.rcl.telephony.entity.RejectReason;

/**
 * ICall is a call between 2 or more parties.<br>
 * Use this interface for starting, accepting, and finishing the call,
 * as well as adding event listeners.
 *
 */
public interface ICall 
extends IListenable<ICallListener>, IParametrizable<ICallParams>, IApplicationDataHolder {

	/**
	 * Initiates a call to a remote party.
	 * @param params the call parameters
	 */
	void start(ICallParams params);
	
	/**
	 * Accepts the incoming call from a remote party.
	 * Used after an appropriate {@link IConnectionEvent} is received.
	 * @param sdpAnswerer SDP parameters of a future call
	 */
	void accept(CallType callType, String sdpAnswerer);
	
	/**
	 * Accepts the incoming call from a remote party.
	 * Used after an appropriate {@link IConnectionEvent} is received.
	 * @param callType a call type to establish the call (video or audio)
	 */
	void accept(CallType callType);
	
	/**
	 * Rejects the incoming call from a remote party.
	 * Used after an appropriate {@link IConnectionEvent} is received.
	 * 
	 * @param reason reject reason
	 */
	void reject(RejectReason reason);
	
	/**
	 * Finishes the call to a remote party.
	 */
	void finish();
	
	/**
	 * Returns the underlying connection.
	 */
	IConnection getConnection();
	
	/**
	 * Returns the unique identifier of the call.
	 */
	String getId();
	
	/**
	 * Sends a DTMF signal.
	 * 
	 * @param digits a string containing DTMF digits
	 */
	void dtmf(String digits);
	
	/**
	 * Transfers a call.
	 * @param toParams transfer destination
	 */
	void transfer(ICallParams toParams);
}
