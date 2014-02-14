package com.rcslabs.rcl.event;

import com.rcslabs.rcl.telephony.event.ICallStartingEvent;

public class CallStartingEvent extends CallEvent implements ICallStartingEvent {
	private Stage stage;

	public CallStartingEvent(Stage stage) {
		super(Type.CALL_STARTING);
		this.stage = stage;
	}

	public Stage getStage() {
		return stage;
	}

	@Override
	public String toString() {
		return "CallStartingEvent [type=" + getEventType() + ", call=" + getCall() + ", stage=" + stage + "]";
	}

	
}
