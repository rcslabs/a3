package com.rcslabs.redis;

/**
 * Created by ykrkn on 13.10.14.
 */
public class DefaultChannel extends Channel {

    public static final String NAME = "default-channel";

    public DefaultChannel(RedisSubscriber subscriber) {
        super(NAME, subscriber);
    }
}
