package com.rcslabs.click2call.service;


import com.rcslabs.click2call.dao.StatDAO;
import com.rcslabs.click2call.entity.CallConsolidatedEntry;
import com.rcslabs.click2call.entity.CallLogEntry;
import com.rcslabs.click2call.entity.ClientLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
@Transactional
public class StatServiceImpl implements StatService {

    private static final Logger log = LoggerFactory.getLogger(StatServiceImpl.class);

    private static final String KEY_FOR_CLIENTS_LOG = "log:clients";
    private static final String KEY_FOR_CALLS_LOG = "log:calls";

    @Autowired
    private StatDAO dao;

    @Autowired
    private RedisTemplate<String, ClientLogEntry> redisClientLogEntryTemplate;

    @Autowired
    private RedisTemplate<String, CallLogEntry> redisCallLogEntryTemplate;

    @Override
    public void pushClientLogEntry(ClientLogEntry item) {
        redisClientLogEntryTemplate.opsForList().rightPush(KEY_FOR_CLIENTS_LOG, item);
        redisClientLogEntryTemplate.convertAndSend(KEY_FOR_CLIENTS_LOG, item);

    }

    @Override
    public void flushClientsLog() {
        log.info("Flush clients log");
        int cnt = 0;
        ListOperations<String, ClientLogEntry> ops = redisClientLogEntryTemplate.opsForList();
        ClientLogEntry entry;
        while(true){
            entry = ops.index(KEY_FOR_CLIENTS_LOG, 0);
            if(null == entry){ break; }
            dao.save(entry);
            ops.leftPop(KEY_FOR_CLIENTS_LOG);
            cnt++;
        }
        log.info("Flushed client log entries: " + cnt);
    }

    @Override
    public void flushCallsLog() {
        log.info("Flush calls log");
        int cnt = 0;
        ListOperations<String, CallLogEntry> ops = redisCallLogEntryTemplate.opsForList();
        CallLogEntry entry;
        while(true){
            entry = ops.index(KEY_FOR_CALLS_LOG, 0);
            if(null == entry){ break; }
            dao.save(entry);
            ops.leftPop(KEY_FOR_CALLS_LOG);
            cnt++;
        }
        log.info("Flushed call log entries: " + cnt);
    }

    @Override
    public void consolidateCalls() {
        log.info("Consolidate calls from log to statistics");
        int cnt = 0;
        // list of objects
        Iterator notConsolidatedCallsIter = dao.findNotConsolidatedCalls().iterator();
        List<CallLogEntry> notConsolidatedCallLogEntries;
        CallConsolidatedEntry consolidatedEntry;
        while( notConsolidatedCallsIter.hasNext() ){
            Object[] tuple = (Object[]) notConsolidatedCallsIter.next();
            //String buttonId = (String)tuple[0];
            String callId = (String)tuple[1];
            notConsolidatedCallLogEntries = dao.getCallLogEntriesByCallId(callId);

            // create one consolidated row from several log-entry rows
            consolidatedEntry = new CallConsolidatedEntry(notConsolidatedCallLogEntries);

            // Achtung! The call entry must be "completed" means it state has "failed" or "finished".
            // Only in this case we can mark an entry rows as consolidated.
            if(consolidatedEntry.isCompleted()){
                dao.save(consolidatedEntry);
                cnt++;
                for(CallLogEntry le : notConsolidatedCallLogEntries){
                    le.setConsolidated(true);
                    dao.update(le);
                }
            }
        }
        log.info("Consolidated calls: " + cnt);
    }

    @Override
    public List<CallConsolidatedEntry> findConsolidatedCalls(Date parsedDate) {
        try {
            return dao.findCallsByDate(parsedDate);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<CallConsolidatedEntry> findConsolidatedCalls(String buttonId, Date parsedDate) {
        try{
            return dao.findCallsByButtonIdAndMonth(buttonId, parsedDate);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, BigInteger> countCallsByMonth(Date date){
        return dao.countCallsByMonth(date);
    }

    @Override
    public void parseRed5Calls(String directoryPath){
        try{
            File dir = new File(directoryPath);

            if(!dir.exists()){
                throw new RuntimeException("Directory " + directoryPath + " not exist");
            }

            for (File nextFile : dir.listFiles())
            {
                if(!nextFile.getName().endsWith("gz")) continue;
                if(dao.isR5LogProcessed(nextFile.getName())) continue;

                log.info("Handle log " + nextFile.getName());

                BufferedReader buffered = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(nextFile))));
                String line;
                int cnt = 0;

                while((line = buffered.readLine()) != null)
                {
                    if(!line.endsWith("CALL_FINISHED")) continue;
                    if(-1 != line.indexOf("call-duration=0")) continue;

                    log.debug("Handle the line " + line);

                    String[] tokens = line.split(", ");

                    CallConsolidatedEntry entry = new CallConsolidatedEntry();

                    String t;
                    String[] kv;
                    int duration = 0;

                    for(int i=0; i<tokens.length; ++i)
                    {
                        t = tokens[i];
                        if(-1 != t.indexOf("call-id")){
                            kv = t.split("=");
                            entry.setCallId(kv[1]);
                            entry.setSipId(kv[1]);
                        }else if(-1 != t.indexOf("app-id")){
                            kv = t.split("=");
                            entry.setButtonId(kv[1]);
                        }else if(-1 != t.indexOf("call-a-number")){
                            kv = t.split("=");
                            entry.setA(kv[1].replaceFirst(" CALL_FINISHED", ""));
                        }else if(-1 != t.indexOf("call-b-number")){
                            kv = t.split("=");
                            entry.setB(kv[1]);
                        }else if(-1 != t.indexOf("ip-address")){
                            kv = t.split(" ");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date d = sdf.parse(kv[0] + " " + kv[1]);
                            entry.setFinished(d);
                        }else if(-1 != t.indexOf("call-duration")){
                            kv = t.split("=");
                            duration = Integer.parseInt(kv[1].replaceFirst(" CALL_FINISHED", ""), 10);
                        }
                    }

                    Date d = new Date(entry.getFinished().getTime()-(duration*1000));
                    entry.setStart(d);
                    entry.setStarted(d);
                    entry.setDetails(nextFile.getName());

                    dao.save(entry);

                    cnt++;
                }

                log.info("Processed " + cnt + " lines in " + nextFile.getName());
            }
        } catch (IOException | ParseException | RuntimeException e){
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> findClientSubmitFormEntries(String buttonId, Date parsedDate) {
        try{
            List<ClientLogEntry> list = dao.findClientLogEntriesByButtonIdAndMonth(buttonId, parsedDate);
            HashMap<String, Integer> result = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("d");
            String dayOfMonth;
            for(ClientLogEntry entry : list){
                if(!"SUBMIT_FORM".equals(entry.getType())) continue;
                dayOfMonth = sdf.format(entry.getTimestamp());
                if(!result.containsKey(dayOfMonth)){
                    result.put(dayOfMonth, 0);
                }
                result.put(dayOfMonth, result.get(dayOfMonth)+1);
            }
            return result;
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
