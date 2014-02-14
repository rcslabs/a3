package ru.rcslabs.webcall.server;

import java.util.Collection;

import javax.annotation.PostConstruct;

import ru.rcslabs.webcall.server.app.IAppFactory;
import ru.rcslabs.webcall.server.app.IAppLoader;
import ru.rcslabs.webcall.server.model.Application;

/**
 * Invokes IAppLoader to initiate Webcall application loading.
 *
 */
public class AppLoaderInvoker {
	
	private IAppLoader appLoader;
	
	private IAppFactory appFactory;

	private Collection<Application> loadedApps;
	
	public AppLoaderInvoker(IAppLoader appLoader, IAppFactory appFactory) {
		this.appLoader = appLoader;
		this.appFactory = appFactory;
	}

	@PostConstruct
	public void postConstruct() {
		loadedApps = appLoader.loadApps(appFactory);
	}

	public Collection<Application> getLoadedApps() {
		return loadedApps;
	}
}
