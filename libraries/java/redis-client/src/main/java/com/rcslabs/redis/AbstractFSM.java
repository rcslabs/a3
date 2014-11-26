package com.rcslabs.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ykrkn on 14.10.14.
 */
abstract class AbstractFSM implements IFSM<AbstractFSM.Event>{

    private static final Logger log = LoggerFactory.getLogger(AbstractFSM.class);

    enum Event{
        CONNECT, CONNECTED, DISCONNECT, DISCONNECTED, CONNECTION_FAILED
    }


    enum State{
        NEW, CONNECT, CONNECTED, DISCONNECT, DISCONNECTED, CONNECTION_FAILED
    }

    private State state = State.NEW;

    public void onEvent(Event event) {
        if(state == State.NEW){
            if(event == Event.CONNECT){
                setState(State.CONNECT);
            }else{
                unhandledEvent(event);
            }
        }else if(state == State.CONNECT){
            if(event == Event.CONNECTION_FAILED){
                setState(State.CONNECTION_FAILED);
            }else if (event == Event.CONNECTED){
                setState(State.CONNECTED);
            }else{
                unhandledEvent(event);
            }
        }else if(state == State.CONNECTED){
            if(event == Event.CONNECTION_FAILED){
                setState(State.CONNECTION_FAILED);
            }else if (event == Event.DISCONNECT){
                setState(State.DISCONNECT);
            }else{
                unhandledEvent(event);
            }
        }else if(state == State.DISCONNECT){
            if (event == Event.DISCONNECTED){
                setState(State.DISCONNECTED);
            }else{
                unhandledEvent(event);
            }
        }else if(state == State.DISCONNECTED){
            if (event == Event.CONNECT){
                setState(State.CONNECT);
            }else{
                unhandledEvent(event);
            }
        }else if(state == State.CONNECTION_FAILED){
            if (event == Event.CONNECT){
                setState(State.CONNECT);
            }else{
                unhandledEvent(event);
            }
        }
    }

    private void setState(State newState){
        log.info("" + this + " " + state + "->" + newState);
        state = newState;
        switch(state){
            case CONNECTED: onConnected(); break;
            case DISCONNECTED: onDisconnected(); break;
            case CONNECTION_FAILED: onConnectionFailed(); break;
        }
    }

    protected void unhandledEvent(Event event){
        log.warn("" + this + " Unhandled event: " + event + " on state: " + state);
    }
}
