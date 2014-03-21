package com.rcslabs.redis;

public interface ITypedMessage<T> extends IMessage {

    IMessage cloneWithAnyType(T type);

    IMessage cloneWithSameType();

    T getType();
}
