package com.rcslabs.rcl.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.rcl.core.entity.ConnectionParams;
import com.rcslabs.rcl.core.stub.DefaultFactoryDecorator;

public class RclAutomationFactory extends DefaultFactoryDecorator {
	private static Logger log = LoggerFactory.getLogger(RclAutomationFactory.class);

	private final RclAutomationGlobalParams globalParams;

	public RclAutomationFactory(IRclFactory rclFactory, RclAutomationGlobalParams globalParams) {
		super(rclFactory);
		this.globalParams = globalParams;		
	}
	
	public void start() {
		//automatically open all the specified connections
		for(String phoneNumber : globalParams.getPhoneNumbers()) {
			ConnectionParams cnParams = new ConnectionParams();
			cnParams.setPhoneNumber(phoneNumber);
			cnParams.setPassword(globalParams.getPassword());
			log.info("Automatically opening connection for phone number {}", phoneNumber);
			newConnection().open(cnParams);
		}
	}

	@Override
	public IConnection newConnection() {
		return new RclAutomationConnection(super.newConnection());
	}
}
