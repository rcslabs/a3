package com.rcslabs.rcl.event;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.core.entity.ErrorInfo;

public class Event {
	private IConnection connection;
	private ErrorInfo errorInfo;

	public Event() {
	}

	public void setConnection(IConnection connection) {
		this.connection = connection;
	}

	public IConnection getConnection() {
		return connection;
	}

	public void setErrorInfo(ErrorInfo errorInfo) {
		this.errorInfo = errorInfo;
	}

	public ErrorInfo getErrorInfo() {
		return errorInfo;
	}

}