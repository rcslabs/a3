package com.rcslabs.rcl.telephony.media.stub;

import com.rcslabs.rcl.telephony.media.IMediaCall;
import com.rcslabs.rcl.telephony.media.IRclMediaFactory;

public class DefaultRclMediaFactory implements IRclMediaFactory {

	@Override
	public IMediaCall newMediaCall() {
		return new DefaultMediaCall();
	}

	@Override
	public void dispose() {
	}

}
