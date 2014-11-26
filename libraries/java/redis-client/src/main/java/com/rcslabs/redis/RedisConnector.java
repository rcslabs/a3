package com.rcslabs.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.SafeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by ykrkn on 23.06.14.
 */
public class RedisConnector implements IPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisConnector.class);

    private final JedisPool pool;

    private RedisSubscriber subscriber;

    private Map<String, IMessageSerializer> serializerMap;

    private final String DEFAULT_SERIALIZER_KEY = "DEFAULT_SERIALIZER_KEY";

    private ThreadPoolExecutor es;

    public RedisConnector(JedisPool pool) {
        this.pool = pool;
        serializerMap = new HashMap<String, IMessageSerializer>();
        es = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        subscriber = new RedisSubscriber(pool, this);
    }

    public void subscribe() throws MessagingException {
        es.execute(subscriber);
    }

    public void dispose() {
        subscriber.dispose();
        es.purge();
    }

    void onConnectionFailed() {
        es.execute(subscriber);
    }

    public void addMessageListener(String channel, IMessageListener listener) {
        subscriber.addMessageListener(channel, listener);
    }

    public void removeMessageListener(String channel, IMessageListener listener) {
        subscriber.removeMessageListener(channel, listener);
    }

    public void removeAllListeners(String channel) {
        subscriber.removeAllListeners(channel);
    }

    public void addSerializer(IMessageSerializer obj) throws MessagingException {
        addSerializer(obj, DEFAULT_SERIALIZER_KEY);
    }

    public void addSerializer(IMessageSerializer obj, String channel) throws MessagingException {
        if (serializerMap.containsKey(channel)) {
            throw new MessagingException(MessagingException.SERIALIZER_EXIST, channel);
        }
        serializerMap.put(channel, obj);
    }

    public IMessageSerializer getSerializer() throws MessagingException {
        if (serializerMap.containsKey(DEFAULT_SERIALIZER_KEY)) {
            return getSerializer(DEFAULT_SERIALIZER_KEY);
        } else {
            throw new MessagingException(MessagingException.SERIALIZER_ABSENT);
        }
    }

    public IMessageSerializer getSerializer(String channel) throws MessagingException {
        if (serializerMap.containsKey(channel)) {
            return serializerMap.get(channel);
        } else if (serializerMap.containsKey(DEFAULT_SERIALIZER_KEY)) {
            return getSerializer(DEFAULT_SERIALIZER_KEY);
        } else {
            throw new MessagingException(MessagingException.SERIALIZER_ABSENT, channel);
        }
    }

    @Override
    public void publish(String channel, IMessage message) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.publish(SafeEncoder.encode(channel), getSerializer(channel).serialize(message));
            log.info("publish " + channel + " : " + message);
            pool.returnResource(jedis);
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
        } catch (JedisConnectionException e) {
            try {
                pool.returnBrokenResource(jedis);
            } catch (JedisException e2) {
                // Could not return the resource to the pool? Who cares?
            }
        }
    }

    public Jedis getResource() {
        return pool.getResource();
    }

    public void returnResource(Jedis jedis) {
        if(jedis == null) return;
        try{
            pool.returnResource(jedis);
        } catch (JedisConnectionException e) {
            try {
                pool.returnBrokenResource(jedis);
            } catch (JedisException e2) {
                // Could not return the resource to the pool? Who cares?
            }
        }
    }
}
