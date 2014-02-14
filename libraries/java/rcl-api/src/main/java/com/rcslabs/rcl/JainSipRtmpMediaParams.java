package com.rcslabs.rcl;

import com.rcslabs.rcl.telephony.entity.ICustomMediaParams;
import com.rcslabs.rcl.telephony.media.IRtmpMediaParams;
import com.rcslabs.rcl.telephony.media.entity.RtmpUrl;

public class JainSipRtmpMediaParams implements IRtmpMediaParams {
	private RtmpUrl rtmpPublishUrlAudio;
	private RtmpUrl rtmpPublishUrlVideo;
	private RtmpUrl rtmpPlayUrlAudio;
	private RtmpUrl rtmpPlayUrlVideo;
	
	public JainSipRtmpMediaParams(
			RtmpUrl rtmpPlayUrlAudio, 
			RtmpUrl rtmpPublishUrlAudio,
			RtmpUrl rtmpPlayUrlVideo,
			RtmpUrl rtmpPublishUrlVideo) 
	{
		this.rtmpPlayUrlAudio = rtmpPlayUrlAudio;
		this.rtmpPublishUrlAudio = rtmpPublishUrlAudio;
		this.rtmpPlayUrlVideo = rtmpPlayUrlVideo;
		this.rtmpPublishUrlVideo = rtmpPublishUrlVideo;
	}

	public void setRtmpPlayUrlAudio(RtmpUrl rtmpPlayUrlAudio) {
		this.rtmpPlayUrlAudio = rtmpPlayUrlAudio;
	}

	@Override
	public RtmpUrl getRtmpPlayUrlAudio() {
		return rtmpPlayUrlAudio;
	}

	public void setRtmpPlayUrlVideo(RtmpUrl rtmpPlayUrlVideo) {
		this.rtmpPlayUrlVideo = rtmpPlayUrlVideo;
	}

	@Override
	public RtmpUrl getRtmpPlayUrlVideo() {
		return rtmpPlayUrlVideo;
	}

	@Override
	public RtmpUrl getRtmpPublishUrlAudio() {
		return rtmpPublishUrlAudio;
	}

	public void setRtmpPublishUrlAudio(RtmpUrl rtmpPublishUrlAudio) {
		this.rtmpPublishUrlAudio = rtmpPublishUrlAudio;
	}

	@Override
	public RtmpUrl getRtmpPublishUrlVideo() {
		return rtmpPublishUrlVideo;
	}

	public void setRtmpPublishUrlVideo(RtmpUrl rtmpPublishUrlVideo) {
		this.rtmpPublishUrlVideo = rtmpPublishUrlVideo;
	}

	@Override
	public ICustomMediaParams clone() {
		try {
			return (JainSipRtmpMediaParams)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
