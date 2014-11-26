package com.rcslabs.redis;

/**
 * Created by ykrkn on 14.10.14.
 */
class FSMDecorator extends AbstractFSM {

    private IFSM decoratee;

    public FSMDecorator(IFSM decoratee){
        this.decoratee = decoratee;
    }

    @Override
    public void onConnected() {
        decoratee.onConnected();
    }

    @Override
    public void onDisconnected() {
        decoratee.onDisconnected();
    }

    @Override
    public void onConnectionFailed() {
        decoratee.onConnectionFailed();
    }
}
