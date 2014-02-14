package com.rcslabs.rcl;

import com.rcslabs.rcl.telephony.entity.ICustomCallParams;

/**
 * Custom params for defining max call duration for video and
 * audio call, as well as the notification time.
 *
 */
public interface ICallDurationParams extends ICustomCallParams {

	long getVideoCallMaxDuration();
	long getAudioCallMaxDuration();
	long getNotificationTime();
}
