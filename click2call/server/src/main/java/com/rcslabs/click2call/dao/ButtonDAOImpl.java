package com.rcslabs.click2call.dao;

import com.rcslabs.click2call.entity.ButtonEntry;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ButtonDAOImpl implements ButtonDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public String getEmailByButtonId(String id) {
        String q = "SELECT ap.value AS email FROM application a " +
                   "LEFT JOIN applicationparameter ap " +
                   "ON (ap.application_id=a.id) " +
                   "WHERE a.name=:id AND ap.name='email'";
        SQLQuery sql = getSession().createSQLQuery(q);
        sql.setString("id", id);
        List rows = sql.list();
        if(0 == rows.size()){ return null; }
        return (String)rows.get(0);
    }

    @Override
    public List<ButtonEntry> getButtonList() {
        return getSession().createQuery("from ButtonEntry order by title").list();
    }

    @Override
    public ButtonEntry getButtonByTitle(String value) {
        List rows = getSession().createQuery("from ButtonEntry where title=:title")
                .setString("title", value).list();
        if(0 == rows.size()){ return null; }
        return (ButtonEntry)rows.get(0);
    }
}
