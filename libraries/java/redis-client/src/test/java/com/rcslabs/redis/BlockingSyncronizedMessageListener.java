package com.rcslabs.redis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sx on 17.10.14.
 */
public class BlockingSyncronizedMessageListener extends BlockingMessageListener {

    private Object monitor = new Object();

    public BlockingSyncronizedMessageListener(int delay, AtomicInteger cnt) {
        super(delay, cnt);
    }

    @Override
    public void onMessage(String channel, IMessage message) {
        synchronized (monitor) {
            super.onMessage(channel, message);
        }
    }
}
