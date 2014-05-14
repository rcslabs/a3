package com.rcslabs.click2call.dao;

import com.rcslabs.click2call.entity.CallConsolidatedEntry;
import com.rcslabs.click2call.entity.CallLogEntry;
import com.rcslabs.click2call.entity.ClientLogEntry;

import java.math.BigInteger;
import java.text.ParseException;
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
    List<CallConsolidatedEntry> findCallsByDate(Date date) throws ParseException;
    List<CallConsolidatedEntry> findCallsByButtonIdAndMonth(String buttonId, Date date) throws ParseException;
    List<ClientLogEntry> findClientLogEntriesByButtonIdAndMonth(String buttonId, Date date) throws ParseException;

    // needs to parse old red5calls logs
    // filename is such as '20140101.be1.gz' and this name stored in 'details' column
    // check enties with filename as 'details'
    boolean isR5LogProcessed(String filename);
}
