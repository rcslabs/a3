package com.rcslabs.rcl.telephony.stub;

import java.util.List;

import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;

public abstract class DefaultTelephonyServiceDecorator implements ITelephonyService {
	
	protected final ITelephonyService rclTelephonyService;

	public DefaultTelephonyServiceDecorator(ITelephonyService rclTelephonyService) {
		this.rclTelephonyService = rclTelephonyService;
	}

	public List<ICall> getCalls() {
		return rclTelephonyService.getCalls();
	}

	public ICall newCall() {
		return rclTelephonyService.newCall();
	}

	public void addListener(ITelephonyServiceListener listener) {
		rclTelephonyService.addListener(listener);
	}

	public void removeListener(ITelephonyServiceListener listener) {
		rclTelephonyService.removeListener(listener);
	}
	
	@Override
	public ICall findCall(String id) {
		return rclTelephonyService.findCall(id);
	}

}
