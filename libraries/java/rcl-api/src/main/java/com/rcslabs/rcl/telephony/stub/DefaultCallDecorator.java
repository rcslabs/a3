package com.rcslabs.rcl.telephony.stub;

import com.rcslabs.rcl.core.IConnection;
import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.ICallListener;
import com.rcslabs.rcl.telephony.entity.CallType;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.entity.MediaParams;
import com.rcslabs.rcl.telephony.entity.RejectReason;

public abstract class DefaultCallDecorator implements ICall {
	
	protected final ICall rclCall;

	public DefaultCallDecorator(ICall rclCall) {
		this.rclCall = rclCall;
	}

//	public void accept(MediaParams params) {
//		rclCall.accept(params);
//	}
	
	@Override
	public void accept(CallType callType) {
		rclCall.accept(callType);
	}

	public void dtmf(String dtmf) {
		rclCall.dtmf(dtmf);
	}

	public void finish() {
		rclCall.finish();
	}

	public IConnection getConnection() {
		return rclCall.getConnection();
	}

	public String getId() {
		return rclCall.getId();
	}

	public void reject(RejectReason reason) {
		rclCall.reject(reason);
	}

	public void start(ICallParams params) {
		rclCall.start(params);
	}

	public void addListener(ICallListener listener) {
		rclCall.addListener(listener);
	}

	public void removeListener(ICallListener listener) {
		rclCall.removeListener(listener);
	}

	public ICallParams getParams() {
		return rclCall.getParams();
	}

	public Object getApplicationData(Object key) {
		return rclCall.getApplicationData(key);
	}

	public void setApplicationData(Object key, Object data) {
		rclCall.setApplicationData(key, data);
	}
	
	public void removeApplicationData(Object key) {
		rclCall.removeApplicationData(key);
	}
	
	@Override
	public void transfer(ICallParams toParams) {
		rclCall.transfer(toParams);
	}

}
