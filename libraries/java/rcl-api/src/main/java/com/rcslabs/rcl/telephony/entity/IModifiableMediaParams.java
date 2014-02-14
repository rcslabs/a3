package com.rcslabs.rcl.telephony.entity;

import java.util.Set;


public interface IModifiableMediaParams extends IMediaParams {

	void setLocalAudioPort(int localAudioPort);

	void setLocalVideoPort(int localVideoPort);

	void setMediaType(MediaType mediaType);

	void setRemoteAudioPort(int remoteAudioPort);

	void setRemoteVideoPort(int remoteVideoPort);

	void setRemoteAddress(String remoteAddress);

	void setAudioMediaFormats(Set<Integer> modifiableAudioMediaFormats);

	void setVideoMediaFormats(Set<Integer> modifiableVideoMediaFormats);

	void setPtime(Integer ptime);

}
