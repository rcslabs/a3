package com.rcslabs.a3.stat;

import java.util.Date;
import java.util.List;

public interface StatDAO {

    List<CallLogEntry> getCallLogEntriesByCallId(String id);
    CallLogEntry save(CallLogEntry item);
    CallLogEntry update(CallLogEntry item);

    ClientLogEntry save(ClientLogEntry item);
    CallConsolidatedEntry save(CallConsolidatedEntry item);
    List<CallLogEntry> findNotConsolidatedCalls();
    List<CallConsolidatedEntry> findCallsByDate(Date date);

}
