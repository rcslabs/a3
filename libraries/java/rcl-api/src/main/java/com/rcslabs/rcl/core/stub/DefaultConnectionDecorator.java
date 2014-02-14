package com.rcslabs.rcl.core.stub;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.IConnectionListener;
import com.rcslabs.rcl.core.IService;
import com.rcslabs.rcl.core.IAddressUri;
import com.rcslabs.rcl.core.entity.IConnectionParams;
import com.rcslabs.rcl.exception.ServiceNotEnabledException;

public abstract class DefaultConnectionDecorator implements IConnection {

	protected final IConnection rclConnection;

	public DefaultConnectionDecorator(IConnection rclConnection) {
		this.rclConnection = rclConnection;
	}

	public void close() {
		rclConnection.close();
	}

	public String getId() {
		return rclConnection.getId();
	}

	public void open(IConnectionParams params) {
		rclConnection.open(params);
	}

	public void addListener(IConnectionListener listener) {
		rclConnection.addListener(listener);

	}

	public void removeListener(IConnectionListener listener) {
		rclConnection.removeListener(listener);
	}

	public IConnectionParams getParams() {
		return rclConnection.getParams();
	}

	public Object getApplicationData(Object key) {
		return rclConnection.getApplicationData(key);
	}

	public void setApplicationData(Object key, Object data) {
		rclConnection.setApplicationData(key, data);
	}
	
	public void removeApplicationData(Object key) {
		rclConnection.removeApplicationData(key);
	}

	public <T extends IService> T getService(Class<T> cls) throws ServiceNotEnabledException {
		return rclConnection.getService(cls);
	}
	
	public <T extends IService> boolean isServiceEnabled(Class<T> cls) {
		return rclConnection.isServiceEnabled(cls);
	}
	
	public IAddressUri getUri() {
		return rclConnection.getUri();
	}

}
