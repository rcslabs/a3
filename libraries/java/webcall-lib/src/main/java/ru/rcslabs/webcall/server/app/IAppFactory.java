package ru.rcslabs.webcall.server.app;

import org.springframework.context.ApplicationContext;

import ru.rcslabs.webcall.server.model.Application;

/**
 * Creates all application-relates beans and contexts.
 *
 */
public interface IAppFactory {

	ApplicationContext loadAppContext(Application app);
	
	void unloadAppContext(String appName);
	
	IWebcallApplication getAppContext(String appName);
}
