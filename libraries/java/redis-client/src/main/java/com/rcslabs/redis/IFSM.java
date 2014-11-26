package com.rcslabs.redis;

/**
 * Created by ykrkn on 14.10.14.
 */
interface IFSM<E> {

    void onEvent(E event);

    void onConnected();

    void onDisconnected();

    void onConnectionFailed();
}
