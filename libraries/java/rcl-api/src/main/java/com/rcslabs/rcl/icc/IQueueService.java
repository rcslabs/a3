package com.rcslabs.rcl.icc;

import java.net.URL;

import com.rcslabs.rcl.core.IService;

public interface IQueueService extends IService {
	
	URL getAnnouncement();
	
	int getPositionInQueue();
	
	long getRemainingTime();
}
