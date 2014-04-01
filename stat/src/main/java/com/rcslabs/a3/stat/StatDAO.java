package com.rcslabs.a3.stat;

import java.util.Date;
import java.util.List;

public interface StatDAO {

    CallLogEntry save(CallLogEntry item);
    CallLogEntry update(CallLogEntry item);
    List<CallLogEntry> getCallLogEntriesByCallId(String id);
    List<CallLogEntry> findNotConsolidatedCalls();

    ClientLogEntry save(ClientLogEntry item);

    CallConsolidatedEntry save(CallConsolidatedEntry item);
    List<CallConsolidatedEntry> findCallsByDate(Date date);
    List<CallConsolidatedEntry> findCallsByButtonIdAndMonth(String buttonId, Date date);

    List<ButtonEntry> getButtonList();
}
