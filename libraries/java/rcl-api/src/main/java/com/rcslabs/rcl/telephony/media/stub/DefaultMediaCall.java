package com.rcslabs.rcl.telephony.media.stub;

import com.rcslabs.rcl.telephony.entity.IMediaParams;
import com.rcslabs.rcl.telephony.entity.IModifiableMediaParams;
import com.rcslabs.rcl.telephony.entity.MediaParams;
import com.rcslabs.rcl.telephony.media.IMediaCall;

public class DefaultMediaCall implements IMediaCall {
	
	private IModifiableMediaParams params = new MediaParams();
	
	protected void setMediaParams(IModifiableMediaParams params) {
		this.params = params;
	}
	
	protected IModifiableMediaParams getMediaParams() {
		return params;
	}

	@Override
	public void changeIncoming(IModifiableMediaParams newParams) {
	}

	@Override
	public void changeOutgoing(IModifiableMediaParams newParams) {
	}

	@Override
	public void startIncoming(IModifiableMediaParams params) {
		this.params = params;
	}

	@Override
	public void startOutgoing(IModifiableMediaParams params) {
	}

	@Override
	public void stop() {
	}

	@Override
	public IMediaParams getParams() {
		return params;
	}

	@Override
	public MediaParams getParamsCopy() {
		return (MediaParams)params.clone();
	}

	@Override
	public void dtmf(String digits) {
	}

}
