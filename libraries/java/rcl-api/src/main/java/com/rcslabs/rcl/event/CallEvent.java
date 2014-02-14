package com.rcslabs.rcl.event;

import com.rcslabs.rcl.telephony.ICall;
import com.rcslabs.rcl.telephony.entity.ISdpObject;
import com.rcslabs.rcl.telephony.event.ICallEvent;

public class CallEvent extends Event implements ICallEvent {

	private final Type type;
	private ICall call;
    private ISdpObject sdp;

    public CallEvent(Type type, ISdpObject sdp) {
        this(type);
        this.sdp = sdp;
    }

	public CallEvent(Type type) {
		this.type = type;
	}

	public ICall getCall() {
		return call;
	}
	
	public void setCall(ICall call) {
		this.call = call;
		setConnection(call.getConnection());
	}

	public Type getEventType() {
		return type;
	}

    @Override
    public ISdpObject getSdpObject() {
        return sdp;
    }

    @Override
	public String toString() {
		return "CallEvent [type=" + type + ", call=" + call + "]";
	}
}
