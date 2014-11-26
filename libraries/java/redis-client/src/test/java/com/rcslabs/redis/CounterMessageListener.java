package com.rcslabs.redis;

/**
 * Created by ykrkn on 14.10.14.
 */
public class CounterMessageListener implements IMessageListener {

    public int getMessagesNum() {
        return cnt;
    }

    int cnt = 0;

    @Override
    public void onMessage(String channel, IMessage message) {
        System.out.println("received " + channel + " : " + message);
        cnt++;
    }
}
