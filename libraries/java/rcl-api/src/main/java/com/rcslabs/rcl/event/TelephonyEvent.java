package com.rcslabs.rcl.event;

import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;

/**
 * Event from {@link ITelephonyService}
 *
 */
public class TelephonyEvent extends Event implements ITelephonyEvent {
	private final Type type;
	private JainSipCall call;

	public TelephonyEvent(Type type) {
		this.type = type;
	}

	@Override
	public Type getEventType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("TelephonyEvent [type=%s, errorInfo=%s]",
				type, getErrorInfo());
	}

	public void setCall(JainSipCall call) {
		this.call = call;
	}

	@Override
	public ICall getCall() {
		return call;
	}
}
