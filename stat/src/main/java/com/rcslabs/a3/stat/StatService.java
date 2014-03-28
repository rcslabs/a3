package com.rcslabs.a3.stat;


public interface StatService {
    void pushClientLogEntry(ClientLogEntry item);
    void flushClientsLog();
    void flushCallsLog();
}
