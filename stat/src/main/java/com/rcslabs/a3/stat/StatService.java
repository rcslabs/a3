package com.rcslabs.a3.stat;


import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StatService {
    void pushClientLogEntry(ClientLogEntry item);
    void flushClientsLog();
    void flushCallsLog();
    void consolidateCalls();

    Map<String, String> getButtons();
    List<CallConsolidatedEntry> findConsolidatedCalls(Date parsedDate);
    List<CallConsolidatedEntry> findConsolidatedCalls(String buttonId, Date parsedDate);

    Map<String, BigInteger> countCallsByMonth(Date date);
}
