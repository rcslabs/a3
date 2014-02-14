package com.rcslabs.rcl.event;

import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.event.ICallTransferEvent;

public class CallTransferEvent extends CallEvent implements ICallTransferEvent {

	private final ICallParams transferParams;

	public CallTransferEvent(Type eventType, ICallParams params) {
		super(eventType);
		this.transferParams = params.clone();
	}

	@Override
	public ICallParams getTransferParams() {
		return transferParams;
	}

}
