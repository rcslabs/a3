package com.rcslabs.a3.stat;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StatDAO {

    CallLogEntry save(CallLogEntry item);
    CallLogEntry update(CallLogEntry item);
    List<CallLogEntry> getCallLogEntriesByCallId(String id);
    List<CallLogEntry> findNotConsolidatedCalls();
    ClientLogEntry save(ClientLogEntry item);
    CallConsolidatedEntry save(CallConsolidatedEntry item);
    Map<String, BigInteger> countCallsByMonth(Date date);
    List<CallConsolidatedEntry> findCallsByDate(Date date);
    List<CallConsolidatedEntry> findCallsByButtonIdAndMonth(String buttonId, Date date);
    List<ButtonEntry> getButtonList();
}
