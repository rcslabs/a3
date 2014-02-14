package com.rcslabs.rcl.telephony.stub;

import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.event.ICallEvent;
import com.rcslabs.rcl.telephony.event.ICallFailedEvent;
import com.rcslabs.rcl.telephony.event.ICallFinishNotificationEvent;
import com.rcslabs.rcl.telephony.event.ICallStartingEvent;
import com.rcslabs.rcl.telephony.event.ICallTransferEvent;

public class DefaultCallListener implements ICallListener {

	@Override
	public void onCallError(ICallEvent event) {
	}

	@Override
	public void onCallFailed(ICallFailedEvent event) {
	}

	@Override
	public void onCallFinished(ICallEvent event) {
	}

	@Override
	public void onCallStarted(ICallEvent event) {
	}

	@Override
	public void onCallStarting(ICallStartingEvent event) {
	}

	@Override
	public void onCallTransfered(ICallTransferEvent event) {
	}

	@Override
	public void onTransferFailed(ICallTransferEvent event) {
	}

	@Override
	public void onCallFinishNotification(ICallFinishNotificationEvent event) {
	}

}
