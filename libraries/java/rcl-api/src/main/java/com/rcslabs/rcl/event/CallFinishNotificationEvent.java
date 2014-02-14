package com.rcslabs.rcl.event;

import com.rcslabs.rcl.telephony.event.ICallFinishNotificationEvent;

public class CallFinishNotificationEvent 
extends CallEvent 
implements ICallFinishNotificationEvent {

	private final long timeBeforeFinish;

	public CallFinishNotificationEvent(long timeBeforeFinish) {
		super(Type.CALL_FINISH_NOTIFICATION);
		this.timeBeforeFinish = timeBeforeFinish;
	}
	
	@Override
	public long getTimeBeforeFinish() {
		return timeBeforeFinish;
	}

}
