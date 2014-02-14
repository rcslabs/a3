package com.rcslabs.rcl.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ITelephonyService;
import com.rcslabs.rcl.telephony.ITelephonyServiceListener;
import com.rcslabs.rcl.telephony.event.ITelephonyEvent;
import com.rcslabs.rcl.telephony.stub.DefaultTelephonyServiceDecorator;

public class RclAutomationTelephonyService 
extends DefaultTelephonyServiceDecorator 
implements ITelephonyServiceListener {
	private static Logger log = LoggerFactory.getLogger(RclAutomationTelephonyService.class);

	public RclAutomationTelephonyService(ITelephonyService rclTelephonyService) {
		super(rclTelephonyService);
		addListener(this);
	}

	@Override
	public void onIncomingCall(ICall call, ITelephonyEvent event) {
		log.info("Automatically accepting incoming call: {}", call.getId());
		call.accept(call.getParams().getCallType()); //automatically accept
	}

	@Override
	public void onCancel(ICall call, ITelephonyEvent event) {
		//do nothing
	}
}
