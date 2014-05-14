package com.rcslabs.click2call.service;


import com.rcslabs.click2call.entity.CallConsolidatedEntry;
import com.rcslabs.click2call.entity.ClientLogEntry;

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

    void parseRed5Calls(String directoryPath);

    Map<String, Integer> findClientSubmitFormEntries(String buttonId, Date parsedDate);
}
