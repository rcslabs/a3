package com.rcslabs.rcl.telephony.event;

import com.rcslabs.rcl.telephony.entity.RejectReason;

public interface ICallFailedEvent extends ICallEvent {

	RejectReason getRejectReason();
}
