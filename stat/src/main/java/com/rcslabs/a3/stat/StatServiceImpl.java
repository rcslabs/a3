package com.rcslabs.a3.stat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
        ClientLogEntry entry = null;
        while(true){
            entry = redisClientLogEntryTemplate.opsForList().leftPop(KEY_FOR_CLIENTS_LOG);
            if(entry == null){ break; }
            dao.save(entry);
        }
    }

    @Override
    public void flushCallsLog() {
        CallLogEntry entry = null;
        while(true){
            entry = redisCallLogEntryTemplate.opsForList().leftPop(KEY_FOR_CALLS_LOG);
            if(entry == null){ break; }
            dao.save(entry);
        }
    }
}
