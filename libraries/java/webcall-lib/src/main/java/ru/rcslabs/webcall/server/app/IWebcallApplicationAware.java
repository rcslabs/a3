package ru.rcslabs.webcall.server.app;

public interface IWebcallApplicationAware {
	void setApplication(IWebcallApplication context);
	IWebcallApplication getApplication();
}
