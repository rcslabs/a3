package ru.rcslabs.webcall.server;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ru.rcslabs.webcall.server.app.IAppFactory;
import ru.rcslabs.webcall.server.app.IAppLoader;
import ru.rcslabs.webcall.server.app.IAppManager;
import ru.rcslabs.webcall.server.app.IWebcallApplication;
import ru.rcslabs.webcall.server.dao.ApplicationDao;
import ru.rcslabs.webcall.server.model.Application;

/**
 * Creates a child context for each created Webcall application.
 * Persists configuration with Hibernate.
 *
 */
public class HibernateAppManager 
implements IAppManager, IAppLoader {
	private static Logger log = LoggerFactory.getLogger(HibernateAppManager.class);
	
	private final SessionFactory sessionFactory;
	private ApplicationDao applicationDao;
	private final IAppFactory appFactory;
	
	public HibernateAppManager(SessionFactory sessionFactory, IAppFactory appFactory) {
		this.sessionFactory = sessionFactory;
		this.appFactory = appFactory;
		applicationDao = new ApplicationDao(sessionFactory);		
	}
	
	@Override
	public Collection<Application> loadApps(IAppFactory appFactory) {
		Collection<Application> ret;
		Session session = sessionFactory.openSession();
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		try {
			ret = list();
			Collection<Application> badApps = new ArrayList<Application>();
			for(Application application : ret) {
				try {
					log.info("Loading Webcall application context for app {}", application.getName());
					appFactory.loadAppContext(application);
				}
				catch(Exception e) {
					log.warn("Failed to load Webcall application context for app " + application.getName(), e);
					badApps.add(application);
				}
			}
			ret.removeAll(badApps);
		}
		finally {
			TransactionSynchronizationManager.unbindResource(sessionFactory);
			session.close();
		}
		
		return ret;
	}
	
	@Override
	@Transactional
	public IWebcallApplication create(final Application app) {
		//save application config (in case of exception, the transaction will be rolled back)
		applicationDao.persist(app);
		
		return appFactory.loadAppContext(app).getBean(IWebcallApplication.class);
	}
	
	@Override
	@Transactional
	public Application findByName(String name) {
		return applicationDao.findByName(name);
	}

	@Override
	@Transactional
	public void remove(String name) {
		appFactory.unloadAppContext(name);
		applicationDao.delete(applicationDao.findByName(name));
	}

	@Override
	@Transactional
	public Collection<Application> list() {
		return applicationDao.listAll();
	}
	
}
