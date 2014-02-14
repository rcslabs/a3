package ru.rcslabs.webcall.server.app;

import java.util.Collection;

import ru.rcslabs.webcall.server.model.Application;

/**
 * Provides CRUD operations on Webcall applications list.
 *
 */
public interface IAppManager {

	IWebcallApplication create(Application app);
	
	void remove(String name);
	
	Collection<Application> list();

	Application findByName(String name);
}
