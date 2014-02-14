package com.rcslabs.rcl.telephony.stub;

import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;

public class DefaultTelephonyServiceListenerDecorator implements ITelephonyServiceListener {
	
	protected final ITelephonyServiceListener rclListener;

	public DefaultTelephonyServiceListenerDecorator(ITelephonyServiceListener rclListener) {
		this.rclListener = rclListener;
	}

	@Override
	public void onIncomingCall(ICall call, ITelephonyEvent event) {
		rclListener.onIncomingCall(call, event);
	}

	@Override
	public void onCancel(ICall call, ITelephonyEvent event) {
		rclListener.onCancel(call, event);
	}

}
