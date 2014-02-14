package ru.rcslabs.webcall.server.dao;

import org.hibernate.SessionFactory;

import ru.rcslabs.webcall.server.model.Application;

import com.rcslabs.hibernate.utils.AbstractHibernateDao;

public class ApplicationDao extends AbstractHibernateDao<Application> {

	public ApplicationDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	protected Class<Application> getEntityClass() {
		return Application.class;
	}

	public Application findByName(String name) {
		return (Application) getWhereCriteria("name", name).uniqueResult();
	}

	public void detach(Application app) {
		getSession().evict(app);
	}

}
