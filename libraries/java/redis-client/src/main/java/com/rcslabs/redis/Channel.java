package com.rcslabs.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ykrkn on 24.06.14.
 */
public class Channel extends AbstractFSM {

    private static final Logger log = LoggerFactory.getLogger(Channel.class);

    private final RedisSubscriber subscriber;

    private final String name;

    private State state = State.NEW;

    private final List<IMessageListener> listeners;

    private final ExecutorService es;

    private final AtomicInteger threadsNum = new AtomicInteger(0);

    public Channel(String name, RedisSubscriber subscriber) {
        this.name = name;
        this.subscriber = subscriber;
        listeners = new ArrayList<IMessageListener>();
        state = State.NEW;
        es = Executors.newCachedThreadPool();
    }

    public String getName() {
        return name;
    }

    public boolean isState(State state) {
        return this.state == state;
    }

    public void addMessageListener(IMessageListener listener) {
        listeners.add(listener);
    }

    public void removeMessageListener(IMessageListener listener) {
        listeners.remove(listener);
        if (!hasListeners() && !hasThreads()) {
            onEvent(AbstractFSM.Event.DISCONNECT);
            subscriber.unsubscribe(getName());
        }
    }

    public void removeAllListeners() {
        listeners.clear();
        if (!hasThreads()) {
            onEvent(AbstractFSM.Event.DISCONNECT);
            subscriber.unsubscribe(getName());
        }
    }

    private boolean hasListeners() {
        return (0 != listeners.size());
    }

    private boolean hasThreads() {
        return (0 != threadsNum.get());
    }

    public void notifyListeners(IMessage message) {
        for (IMessageListener lst : listeners) {
            //lst.onMessage(getName(), message);
            es.execute(new ListenerNotifierExecutor(lst, message));
        }
    }

    private class ListenerNotifierExecutor implements Runnable {

        private IMessageListener listener;
        private IMessage message;

        public ListenerNotifierExecutor(IMessageListener listener, IMessage message) {
            this.listener = listener;
            this.message = message;
        }

        @Override
        public void run() {
            threadsNum.incrementAndGet();
            log.info("notify " + getName() + " : " + message);
            listener.onMessage(getName(), message);
            threadsNum.decrementAndGet();
            if (!hasListeners() && !hasThreads() && state == State.CONNECTED) {
                onEvent(AbstractFSM.Event.DISCONNECT);
                subscriber.unsubscribe(getName());
            }
        }
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed() {
    }

    @Override
    public String toString() {
        return "[Channel " + name + "]";
    }
}
