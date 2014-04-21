package com.rcslabs.a3.stat;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Transactional
public class StatDAOImpl implements StatDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<CallLogEntry> getCallLogEntriesByCallId(String callId) {
        return getSession().createQuery("from CallLogEntry where callId=:callId order by timestamp")
                .setString("callId", callId).list();
    }

    @Override
    public CallLogEntry save(CallLogEntry item) {
        getSession().save(item);
        return item;
    }

    @Override
    public CallLogEntry update(CallLogEntry item) {
        getSession().update(item);
        return item;
    }

    @Override
    public ClientLogEntry save(ClientLogEntry item) {
        getSession().save(item);
        return item;
    }

    @Override
    public CallConsolidatedEntry save(CallConsolidatedEntry item) {
        getSession().save(item);
        return item;
    }

    @Override
    public List findNotConsolidatedCalls() {
        String q = "SELECT DISTINCT ON(call_id) button_id, call_id FROM stat_log_calls WHERE consolidated = false";
        SQLQuery sql = getSession().createSQLQuery(q);
        return sql.list();
    }

    @Override
    public Map<String, BigInteger> countCallsByMonth(Date date) {
        String q = "SELECT DISTINCT ON(date_trunc('day', start_ts)) date_trunc('day', start_ts) AS date, " +
                "COUNT(date_trunc('day', start_ts)) FROM stat_consolidated_calls " +
                "WHERE EXTRACT(MONTH FROM start_ts)=" + (1+date.getMonth()) + " " +
                "GROUP BY date_trunc('day', start_ts)";
        SQLQuery sql = getSession().createSQLQuery(q);
        Iterator iter = sql.list().iterator();
        Map<String, BigInteger> result = new HashMap<String, BigInteger>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        while( iter.hasNext() ){
            Object[] tuple = (Object[]) iter.next();
            result.put(sdf.format((Date)tuple[0]), (BigInteger)tuple[1]);
        }
        return result;
    }

    @Override
    public List<CallConsolidatedEntry> findCallsByDate(Date date) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String d = sdf.format(date); // skip hours
            Date startDate = sdf.parse(d);
            Date endDate = new Date(startDate.getTime()+(86400000));
            Criteria criteria = getSession().createCriteria(CallConsolidatedEntry.class)
                    .add(Restrictions.between("start", startDate, endDate));
            return criteria.list();
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public List<CallConsolidatedEntry> findCallsByButtonIdAndMonth(String buttonId, Date date) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            String d = sdf.format(date); // skip hours, days
            Date startDate = sdf.parse(d);
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.MONTH, 1);
            Date endDate = c.getTime();
            Criteria criteria = getSession().createCriteria(CallConsolidatedEntry.class)
                    .add(Restrictions.between("start", startDate, endDate))
                    .add(Restrictions.eq("buttonId", buttonId)).addOrder(Order.asc("start"));

            return criteria.list();
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public List<ButtonEntry> getButtonList() {
        return getSession().createQuery("from ButtonEntry order by title").list();
    }

}
