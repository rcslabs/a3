package ru.rcslabs.webcall.server.app;

import java.util.Collection;

import ru.rcslabs.webcall.server.model.Application;

/**
 * Loads Webcall applications.
 *
 */
public interface IAppLoader {

	Collection<Application> loadApps(IAppFactory appFactory);
}
