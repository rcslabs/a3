package com.rcslabs.redis;

/**
 * Created by ykrkn on 15.10.14.
 */
public class TestMessage implements IMessage {

    private final Number rnd = Math.random();

    public String toString() {
        return ""+rnd;
    }
}
