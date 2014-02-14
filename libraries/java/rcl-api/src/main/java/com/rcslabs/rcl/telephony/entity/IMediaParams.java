package com.rcslabs.rcl.telephony.entity;

import java.util.Set;

public interface IMediaParams {
	
	int getLocalAudioPort();
	int getLocalVideoPort();
	int getRemoteAudioPort();
	int getRemoteVideoPort();
	String getRemoteAddress();
	void setCustomMediaParams(ICustomMediaParams customMediaParams);
	<T extends ICustomMediaParams> T getCustomMediaParams(Class<T> cls);
	<T extends ICustomMediaParams> void removeCustomMediaParams(Class<T> cls);
	MediaType getMediaType();
	Set<Integer> getAudioMediaFormats();
	Set<Integer> getVideoMediaFormats();
	Integer getPtime();
	IModifiableMediaParams clone();
}