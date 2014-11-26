package com.rcslabs.redis;

/**
 * Created by ykrkn on 15.10.14.
 */
public interface IMessageSerializer {

    byte[] serialize(IMessage message) throws MessagingException;

    IMessage deserialize(byte[] source) throws MessagingException;
}
