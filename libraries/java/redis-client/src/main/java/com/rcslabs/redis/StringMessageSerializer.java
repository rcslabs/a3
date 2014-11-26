package com.rcslabs.redis;

import redis.clients.util.SafeEncoder;

/**
 * Created by ykrkn on 15.10.14.
 */
public class StringMessageSerializer implements IMessageSerializer {

    public IMessage deserialize(final byte[] message) throws MessagingException {
        return new IMessage() {
            @Override
            public String toString() {
                return SafeEncoder.encode(message);
            }
        };
    }

    public byte[] serialize(IMessage message) throws MessagingException {
        return SafeEncoder.encode(message.toString());
    }
}
