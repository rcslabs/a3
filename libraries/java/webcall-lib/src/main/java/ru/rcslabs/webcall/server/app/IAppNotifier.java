package ru.rcslabs.webcall.server.app;

import java.rmi.RemoteException;

import ru.rcslabs.webcall.server.model.Application;

/**
 * Interface to notify server about Application list changes
 * @author vic
 *
 */
public interface IAppNotifier {

	/**
	 * Called to notify server that the Application has been created.
	 * @param app Application instance.
	 * @throws RemoteException
	 */
	void notifyAppCreated(Application app);
	
	/**
	 * Called to notify server that the Application has been created.
	 * @param appName Application name.
	 * @throws RemoteException
	 */
	void notifyAppDestroyed(String appName);
}
