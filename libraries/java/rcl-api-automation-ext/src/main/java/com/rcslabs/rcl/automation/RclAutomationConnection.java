package com.rcslabs.rcl.automation;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IService;
import com.rcslabs.rcl.core.stub.DefaultConnectionDecorator;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;
import com.rcslabs.rcl.telephony.ITelephonyService;

public class RclAutomationConnection extends DefaultConnectionDecorator {
	private RclAutomationTelephonyService telephonyService;

	public RclAutomationConnection(IConnection rclConnection) {
		super(rclConnection);
		try {
			telephonyService = new RclAutomationTelephonyService(super.getService(ITelephonyService.class));
		} catch (ServiceNotEnabledException e) {
			throw new RuntimeException(e);
		}		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IService> T getService(Class<T> cls) throws ServiceNotEnabledException {
		if(ITelephonyService.class.isAssignableFrom(cls)) {
			return (T)telephonyService;
		}
		return super.getService(cls);
	}
}
