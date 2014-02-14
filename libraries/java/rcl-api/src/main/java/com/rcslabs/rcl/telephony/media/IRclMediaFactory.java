package com.rcslabs.rcl.telephony.media;

public interface IRclMediaFactory {

	IMediaCall newMediaCall();
	
	void dispose();
}
