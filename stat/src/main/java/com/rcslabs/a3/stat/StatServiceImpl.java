package com.rcslabs.a3.stat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
            String buttonId = (String)tuple[0];
            String callId = (String)tuple[1];
            notConsolidatedCallLogEntries = dao.getCallLogEntriesByCallId(callId);
            consolidatedEntry = new CallConsolidatedEntry(buttonId, notConsolidatedCallLogEntries);
            dao.save(consolidatedEntry);
            for(CallLogEntry le : notConsolidatedCallLogEntries){
                le.setConsolidated(true);
                dao.update(le);
            }
        }
    }

    @Override
    public List<CallConsolidatedEntry> findConsolidatedCalls(Date parsedDate) {
        return dao.findCallsByDate(parsedDate);
    }
}
