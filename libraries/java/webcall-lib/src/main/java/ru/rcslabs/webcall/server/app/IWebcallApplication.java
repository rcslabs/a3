package ru.rcslabs.webcall.server.app;

import ru.rcslabs.webcall.server.ICallObjectsLifetimeEventsDispatcher;

import com.rcslabs.rcl.core.IRclFactory;

/**
 * An application that provides some client services.
 * A certain piece of logic within a Web application.
 *
 */
public interface IWebcallApplication {

	IRclFactory getRclFactory();

	ICallObjectsLifetimeEventsDispatcher getDispatcher();
	
	Object getClientService(String name);
	
	<T extends IWebcallService> T getService(Class<T> clazz);

	String getName();

}