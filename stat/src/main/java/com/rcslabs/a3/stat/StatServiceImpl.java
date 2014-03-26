package com.rcslabs.a3.stat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatServiceImpl implements StatService {

    private static final String KEY_FOR_CLIENTS_LOG = "log:clients";

    //@Autowired
    //private StatDAO dao;

    @Autowired
    private RedisTemplate<String, ClientLogEntry> redisTemplate;

    @Override
    public void pushClientLogEntry(ClientLogEntry item) {
        redisTemplate.opsForList().rightPush(KEY_FOR_CLIENTS_LOG, item);
        redisTemplate.convertAndSend("log:clients", item);
    }
}
