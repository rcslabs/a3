package com.rcslabs.rcl.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogbackErrorAppender extends AppenderBase<LoggingEvent> {

    static private AsyncTest instance = null;

    public static void setInstance(AsyncTest value){
        instance = value;
    }

    @Override
    protected void append(LoggingEvent e) {
        if(!(e.getLevel() == Level.ERROR || e.getLevel() == Level.WARN)) return;
        instance.setAsFailed();
    }
}
