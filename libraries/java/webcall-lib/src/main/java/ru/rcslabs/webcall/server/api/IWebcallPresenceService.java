package ru.rcslabs.webcall.server.api;

import com.rcslabs.rcl.exception.ServiceNotEnabledException;

public interface IWebcallPresenceService {
	String STATUS_ONLINE = "ONLINE";
	String STATUS_OFFLINE = "OFFLINE";
	String STATUS_AWAY = "AWAY";
	String STATUS_BUSY = "BUSY";
	String MOOD_UNKNOWN = "UNKNOWN";
	String MOOD_HAPPY = "HAPPY";
	String MOOD_EXCITED = "EXCITED";
	String MOOD_ANGRY = "ANGRY";
	String MOOD_ASHAMED = "ASHAMED";
	String MOOD_IN_LOVE = "IN_LOVE";
	String MOOD_SLEEPY = "SLEEPY";
	String MOOD_SAD = "SAD";
	
	void publishStatus(String uid, String status) throws ServiceNotEnabledException;
	void publishMood(String uid, String mood) throws ServiceNotEnabledException;
	void publishNote(String uid, String note) throws ServiceNotEnabledException;

}
