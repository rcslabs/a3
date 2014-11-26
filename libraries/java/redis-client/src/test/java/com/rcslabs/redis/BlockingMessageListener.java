package com.rcslabs.redis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ykrkn on 15.10.14.
 */
public class BlockingMessageListener implements IMessageListener {

    private int delay = 0;

    private final AtomicInteger cnt;

    private String race = "";

    public BlockingMessageListener(int delay, AtomicInteger cnt) {
        this.delay = delay;
        this.cnt = cnt;
    }

    @Override
    public void onMessage(String channel, IMessage message) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) { }
        cnt.incrementAndGet();
        race = race + ".";
        System.out.println(channel + " " + race);
    }

}
