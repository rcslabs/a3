package com.rcslabs.click2call;


import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StatService {
    void pushClientLogEntry(ClientLogEntry item);
    void flushClientsLog();
    void flushCallsLog();
    void consolidateCalls();

    List<CallConsolidatedEntry> findConsolidatedCalls(Date parsedDate);
    List<CallConsolidatedEntry> findConsolidatedCalls(String buttonId, Date parsedDate);

    Map<String, BigInteger> countCallsByMonth(Date date);
}
