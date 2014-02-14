package com.rcslabs.rcl.telephony.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MediaParams implements IModifiableMediaParams, Cloneable {
	
	private int localAudioPort;
	private int localVideoPort;
	private int remoteAudioPort;
	private int remoteVideoPort;
	private String remoteAddress;
	private List<ICustomMediaParams> customMediaParams = new ArrayList<ICustomMediaParams>();
	private MediaType mediaType;
	private Set<Integer> audioMediaFormats = new HashSet<Integer>();
	private Set<Integer> videoMediaFormats = new HashSet<Integer>();
	private Integer ptime;
	
	public int getLocalAudioPort() {
		return localAudioPort;
	}
	
	public void setLocalAudioPort(int localAudioPort) {
		this.localAudioPort = localAudioPort;
	}
	
	public int getLocalVideoPort() {
		return localVideoPort;
	}
	
	public void setLocalVideoPort(int localVideoPort) {
		this.localVideoPort = localVideoPort;
	}

	public void setRemoteAudioPort(int remoteAudioPort) {
		this.remoteAudioPort = remoteAudioPort;
	}

	public int getRemoteAudioPort() {
		return remoteAudioPort;
	}

	public void setRemoteVideoPort(int remoteVideoPort) {
		this.remoteVideoPort = remoteVideoPort;
	}

	public int getRemoteVideoPort() {
		return remoteVideoPort;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setCustomMediaParams(ICustomMediaParams params) {
		Object prevParams = getCustomMediaParams(params.getClass());
		if(prevParams != null) {
			customMediaParams.remove(prevParams);
		}
		customMediaParams.add(params);
	}

	@SuppressWarnings("unchecked")
	public <T extends ICustomMediaParams> T getCustomMediaParams(Class<T> cls) {
		for(Object params : customMediaParams) {
			if(cls.isInstance(params)) {
				return (T)params;
			}
		}
		
		return null;
	}
	
	public <T extends ICustomMediaParams> void removeCustomMediaParams(Class<T> cls) {
		for(Iterator<ICustomMediaParams> iterator = customMediaParams.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if(cls.isInstance(object)) {
				iterator.remove();
			}
		}
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public MediaType getMediaType() {
		return mediaType;
	}
	
	public void setAudioMediaFormats(Set<Integer> mediaFormats) {
		this.audioMediaFormats = new HashSet<Integer>(mediaFormats);
	}
	
	public void setAudioMediaFormats(List<String> mediaFormats) {
		for(String sformat : mediaFormats) {
			this.audioMediaFormats.add(Integer.valueOf(sformat));
		}
	}

	public Set<Integer> getAudioMediaFormats() {
		return Collections.unmodifiableSet(audioMediaFormats);
	}
	
	public Set<Integer> getModifiableAudioMediaFormats() {
		return audioMediaFormats;
	}

	public void setPtime(Integer ptime) {
		this.ptime = ptime;
	}

	public Integer getPtime() {
		return ptime;
	}

	@Override
	public IModifiableMediaParams clone() {
		try {
			MediaParams ret = (MediaParams)super.clone();
			ret.customMediaParams = new ArrayList<ICustomMediaParams>(this.customMediaParams.size());
			for(ICustomMediaParams params : this.customMediaParams) {
				ret.customMediaParams.add(params.clone());
			}
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return String
				.format(
						"MediaParams [customMediaParams=%s, localAudioPort=%s, localVideoPort=%s, mediaFormats=%s, mediaType=%s, ptime=%s, remoteAddress=%s, remoteAudioPort=%s, remoteVideoPort=%s]",
						customMediaParams, localAudioPort, localVideoPort,
						audioMediaFormats, mediaType, ptime, remoteAddress,
						remoteAudioPort, remoteVideoPort);
	}

	@Override
	public Set<Integer> getVideoMediaFormats() {
		return Collections.unmodifiableSet(videoMediaFormats);
	}
	
	public Set<Integer> getModifiableVideoMediaFormats() {
		return videoMediaFormats;
	}

	public void setVideoMediaFormats(Set<Integer> mediaFormats) {
		this.videoMediaFormats = new HashSet<Integer>(mediaFormats);
	}
}
