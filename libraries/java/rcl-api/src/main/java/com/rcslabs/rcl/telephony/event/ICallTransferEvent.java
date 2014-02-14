package com.rcslabs.rcl.telephony.event;

import com.rcslabs.rcl.telephony.entity.ICallParams;

/**
 * Event for call transfer operations.
 *
 */
public interface ICallTransferEvent extends ICallEvent {

	/**
	 * Returns the params of the transfer target.
	 */
	ICallParams getTransferParams();
}
