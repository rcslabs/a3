package com.rcslabs.a3.stat;


import java.util.Date;
import java.util.List;

public interface StatService {
    void pushClientLogEntry(ClientLogEntry item);
    void flushClientsLog();
    void flushCallsLog();
    void consolidateCalls();

    List<CallConsolidatedEntry> findConsolidatedCalls(Date parsedDate);
}
