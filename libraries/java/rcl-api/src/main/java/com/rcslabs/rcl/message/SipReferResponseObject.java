package com.rcslabs.rcl.message;

import javax.sip.ResponseEvent;

import com.rcslabs.rcl.ICallManager;
import com.rcslabs.rcl.JainSipCall;
import com.rcslabs.rcl.event.CallTransferEvent;
import com.rcslabs.rcl.telephony.entity.ICallParams;
import com.rcslabs.rcl.telephony.event.ICallEvent;

public class SipReferResponseObject extends SipResponseObject {

	private final JainSipCall call;
	private final ICallParams referToParams;

	public SipReferResponseObject(
			ICallManager callManager,
			JainSipCall call, 
			ICallParams referToParams) 
	{
		super(callManager);
		this.call = call;
		this.referToParams = referToParams;
		setConnection(call.getConnection());
	}

	@Override
	protected void doOk(ResponseEvent event) {
		call.fireEvent(new CallTransferEvent(ICallEvent.Type.CALL_TRANSFERED, referToParams));
	}
	
	@Override
	protected void doAccepted(ResponseEvent event) {
		//Do nothing. We wait for NOTIFY which will signal us that the call is transfered.
	}
	
}
