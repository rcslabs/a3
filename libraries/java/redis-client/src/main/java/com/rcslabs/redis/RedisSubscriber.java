package com.rcslabs.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.SafeEncoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ykrkn on 23.06.14.
 */
public class RedisSubscriber extends BinaryJedisPubSub implements Runnable, IFSM<AbstractFSM.Event> {

    private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

    private final JedisPool pool;

    private final RedisConnector connector;

    private BinaryJedis jedis;

    private final Map<String, Channel> channels;

    private final DefaultChannel defaultChannel;

    private IFSM decorator;

    public boolean isConnected() {
        return connected;
    }

    private boolean connected;

    public RedisSubscriber(JedisPool pool, RedisConnector connector) {
        this.pool = pool;
        this.connector = connector;
        connected = false;
        decorator = new FSMDecorator(this);
        defaultChannel = new DefaultChannel(this);
        channels = new ConcurrentHashMap<String, Channel>();
    }

    public void addMessageListener(String channel, IMessageListener listener) {
        Channel c = getOrCreate(channel);
        c.addMessageListener(listener);
        if (isConnected()) {
            if (c.isState(AbstractFSM.State.NEW)) {
                c.onEvent(AbstractFSM.Event.CONNECT);
                subscribe(SafeEncoder.encode(channel));
            }
        }
    }

    public void removeMessageListener(String channel, IMessageListener listener) {
        if (!channels.containsKey(channel)) {
            return;
        }
        channels.get(channel).removeMessageListener(listener);
    }

    public void removeAllListeners(String channel) {
        if (!channels.containsKey(channel)) {
            return;
        }
        channels.get(channel).removeAllListeners();
    }

    public void removeAllListeners() {
        for (String channel : channels.keySet()) {
            removeAllListeners(channel);
        }
    }

    private Channel getOrCreate(String name) {
        if (channels.containsKey(name)) {
            return channels.get(name);
        } else {
            Channel channel = new Channel(name, this);
            channels.put(name, channel);
            return channel;
        }
    }

    @Override
    public void run() {
        try {
            onEvent(AbstractFSM.Event.CONNECT);
            defaultChannel.onEvent(AbstractFSM.Event.CONNECT);
            jedis = pool.getResource();
            jedis.subscribe(this, SafeEncoder.encode(defaultChannel.getName())); // blocks here
            pool.returnResource((Jedis) jedis);
        } catch (JedisConnectionException e) {
            try {
                // delay to reconnect 1 sec
                Thread.sleep(1000);
            } catch (InterruptedException e1) { /* nothing happens */ }
            onEvent(AbstractFSM.Event.CONNECTION_FAILED);
        }
    }

    public void dispose() {
        if (connected) {
            onEvent(AbstractFSM.Event.DISCONNECT);
        }
        removeAllListeners();
    }

    synchronized public void unsubscribe(String channel) {
        super.unsubscribe(SafeEncoder.encode(channel));
    }

    @Override
    synchronized public void onEvent(AbstractFSM.Event event) {
        decorator.onEvent(event);
    }

    @Override
    public void onConnected() {
        connected = true;
        for (Channel c : channels.values()) {
            if (c.isState(AbstractFSM.State.NEW)) {
                c.onEvent(AbstractFSM.Event.CONNECT);
                subscribe(SafeEncoder.encode(c.getName()));
            }
        }
    }

    @Override
    public void onDisconnected() {
        connected = false;
    }

    @Override
    public void onConnectionFailed() {
        connected = false;
        try {
            pool.returnBrokenResource((Jedis) jedis);
        } catch (JedisException e) {
            // Could not return the resource to the pool? Who cares?
        }
        connector.onConnectionFailed();
    }


    /* JedisPubSub implemented */


    @Override
    public void onMessage(byte[] channel, byte[] message) {
        String _channel = SafeEncoder.encode(channel);
        log.info("receive " + _channel + " : " + message.length + " bytes");
        if (channels.containsKey(_channel)) {
            IMessage _message = null;
            try {
                _message = (IMessage) connector.getSerializer(_channel).deserialize(message);
                channels.get(_channel).notifyListeners(_message);
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
            }
        }else{
            log.warn("Channel " + _channel + " not subscribed");
        }
    }

    @Override
    public void onSubscribe(byte[] channel, int subscribedChannels) {
        String _channel = SafeEncoder.encode(channel);
        if (_channel.equals(defaultChannel.getName())) {
            defaultChannel.onEvent(AbstractFSM.Event.CONNECTED);
            onEvent(AbstractFSM.Event.CONNECTED);
        } else {
            if (channels.containsKey(_channel)) {
                channels.get(_channel).onEvent(AbstractFSM.Event.CONNECTED);
            }
        }
    }

    @Override
    public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        String _channel = SafeEncoder.encode(channel);
        if (_channel.equals(defaultChannel.getName())) {
            defaultChannel.onEvent(AbstractFSM.Event.DISCONNECTED);
            onEvent(AbstractFSM.Event.DISCONNECTED);
        } else {
            if (channels.containsKey(_channel)) {
                channels.get(_channel).onEvent(AbstractFSM.Event.DISCONNECTED);
                channels.remove(_channel);
            }
        }
    }

    @Override
    public void onPSubscribe(byte[] pattern, int subscribedChannels) {

    }

    @Override
    public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {

    }

    @Override
    public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {

    }
}
