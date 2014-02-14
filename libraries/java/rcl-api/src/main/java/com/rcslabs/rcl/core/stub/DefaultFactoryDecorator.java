package com.rcslabs.rcl.core.stub;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IRclFactory;
import com.rcslabs.util.CollectionUtils.Criteria;
import com.rcslabs.util.CollectionUtils.Operation;

public abstract class DefaultFactoryDecorator implements IRclFactory {
	protected IRclFactory rclFactory;
	
	public DefaultFactoryDecorator(IRclFactory rclFactory) {
		this.rclFactory = rclFactory;
	}

	@Override
	public void dispose() {
		rclFactory.dispose();
	}

	@Override
	public IConnection newConnection() {
		return rclFactory.newConnection();
	}
	
	@Override
	public IConnection findConnection(String id) {
		return rclFactory.findConnection(id);
	}
	
	@Override
	public void forEachConnection(Criteria<IConnection> criteria, Operation<IConnection> operation) {
		rclFactory.forEachConnection(criteria, operation);
	}

}
