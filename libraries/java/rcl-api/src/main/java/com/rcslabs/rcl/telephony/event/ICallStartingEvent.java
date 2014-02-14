package com.rcslabs.rcl.telephony.event;

public interface ICallStartingEvent extends ICallEvent {
	
	public enum Stage {
		INITIALIZING,
		AUTHENTICATING,
		TRYING,
		RINGING, 
		SESSION_PROGRESS
	}
	
	Stage getStage();
}
