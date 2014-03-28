package com.rcslabs.a3.stat;

public interface StatDAO {

    CallLogEntry save(CallLogEntry item);
    ClientLogEntry save(ClientLogEntry item);
}
