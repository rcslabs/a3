package com.rcslabs.redis;

/**
 * Created by ykrkn on 13.10.14.
 */
public interface IPublisher {

    void publish(String channel, IMessage message);
}
