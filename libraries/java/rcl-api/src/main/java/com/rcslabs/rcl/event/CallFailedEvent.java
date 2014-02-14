package com.rcslabs.rcl.event;

import com.rcslabs.rcl.telephony.entity.RejectReason;
import com.rcslabs.rcl.telephony.event.ICallFailedEvent;

public class CallFailedEvent extends CallEvent implements ICallFailedEvent {

	private final RejectReason rejectReason;

	public CallFailedEvent(RejectReason rejectReason) {
		super(Type.CALL_FAILED);
		this.rejectReason = rejectReason;
	}

	public RejectReason getRejectReason() {
		return rejectReason;
	}

}
