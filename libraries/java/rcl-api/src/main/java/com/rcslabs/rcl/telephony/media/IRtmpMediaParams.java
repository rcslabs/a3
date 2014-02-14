package com.rcslabs.rcl.telephony.media;

import com.rcslabs.rcl.telephony.entity.ICustomMediaParams;
import com.rcslabs.rcl.telephony.media.entity.RtmpUrl;

/**
 * Media parameters for RTMP playback (Flash streaming).
 *
 */
public interface IRtmpMediaParams extends ICustomMediaParams {

	/**
	 * Returns an URL for RTMP audio publishing.
	 */
	RtmpUrl getRtmpPublishUrlAudio();
	
	/**
	 * Returns an URL for RTMP video publishing.
	 */
	RtmpUrl getRtmpPublishUrlVideo();
	
	/**
	 * Returns an URL for audio part of RTMP play stream.
	 * 
	 * @return an URL or null, if there is a combined audio-video play stream (see {@link getRtmpPlayUrl()})
	 */
	RtmpUrl getRtmpPlayUrlAudio();
	
	/**
	 * Returns an URL for video part of RTMP play stream.
	 * 
	 * @return an URL or null, if there is a combined audio-video play stream (see {@link getRtmpPlayUrl()})
	 */
	RtmpUrl getRtmpPlayUrlVideo();
}