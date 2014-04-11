package com.rcslabs.a3.stat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
public class StatServiceImpl implements StatService {

    private static final String KEY_FOR_CLIENTS_LOG = "log:clients";
    private static final String KEY_FOR_CALLS_LOG = "log:calls";

    @Autowired
    private StatDAO dao;

    @Autowired
    private RedisTemplate<String, ClientLogEntry> redisClientLogEntryTemplate;

    @Autowired
    private RedisTemplate<String, CallLogEntry> redisCallLogEntryTemplate;

    @Override
    public void pushClientLogEntry(ClientLogEntry item) {
        redisClientLogEntryTemplate.opsForList().rightPush(KEY_FOR_CLIENTS_LOG, item);
        redisClientLogEntryTemplate.convertAndSend(KEY_FOR_CLIENTS_LOG, item);

    }

    @Override
    public void flushClientsLog() {
        ListOperations<String, ClientLogEntry> ops = redisClientLogEntryTemplate.opsForList();
        ClientLogEntry entry;
        while(true){
            entry = ops.index(KEY_FOR_CLIENTS_LOG, 0);
            if(null == entry){ break; }
            dao.save(entry);
            ops.leftPop(KEY_FOR_CLIENTS_LOG);
        }
    }

    @Override
    public void flushCallsLog() {
        ListOperations<String, CallLogEntry> ops = redisCallLogEntryTemplate.opsForList();
        CallLogEntry entry;
        while(true){
            entry = ops.index(KEY_FOR_CALLS_LOG, 0);
            if(null == entry){ break; }
            dao.save(entry);
            ops.leftPop(KEY_FOR_CALLS_LOG);
        }
    }

    @Override
    public void consolidateCalls() {
        // list of objects
        Iterator notConsolidatedCallsIter = dao.findNotConsolidatedCalls().iterator();
        List<CallLogEntry> notConsolidatedCallLogEntries;
        CallConsolidatedEntry consolidatedEntry;
        while( notConsolidatedCallsIter.hasNext() ){
            Object[] tuple = (Object[]) notConsolidatedCallsIter.next();
            //String buttonId = (String)tuple[0];
            String callId = (String)tuple[1];
            notConsolidatedCallLogEntries = dao.getCallLogEntriesByCallId(callId);

            // create one consolidated row from several log-entry rows
            consolidatedEntry = new CallConsolidatedEntry(notConsolidatedCallLogEntries);

            // Achtung! The call entry must be "completed" means it state has "failed" or "finished".
            // Only in this case we can mark an entry rows as consolidated.
            if(consolidatedEntry.isCompleted()){
                dao.save(consolidatedEntry);
                for(CallLogEntry le : notConsolidatedCallLogEntries){
                    le.setConsolidated(true);
                    dao.update(le);
                }
            }
        }
    }

    @Override
    public Map<String, String> getButtons() {
        List<ButtonEntry> list = dao.getButtonList();
        HashMap<String, String> res = new LinkedHashMap<String, String>();
        for(ButtonEntry be : list){
            res.put(be.getButtonId(), be.getTitle());
        }
        return res;
    }

    @Override
    public List<CallConsolidatedEntry> findConsolidatedCalls(Date parsedDate) {
        return dao.findCallsByDate(parsedDate);
    }

    @Override
    public List<CallConsolidatedEntry> findConsolidatedCalls(String buttonId, Date parsedDate) {
        return dao.findCallsByButtonIdAndMonth(buttonId, parsedDate);
    }

    @Override
    public Map<String, BigInteger> countCallsByMonth(Date date){
        return dao.countCallsByMonth(date);
    }
}
