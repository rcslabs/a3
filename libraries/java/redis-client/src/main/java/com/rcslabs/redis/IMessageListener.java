package com.rcslabs.redis;

/**
 * Created by ykrkn on 23.06.14.
 */
public interface IMessageListener {

    void onMessage(String channel, IMessage message);
}
