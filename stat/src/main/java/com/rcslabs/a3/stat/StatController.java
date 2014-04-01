package com.rcslabs.a3.stat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Controller()
@RequestMapping("/")
public class StatController {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    @Autowired
    private StatService service;

    @ResponseBody
    @RequestMapping("/push")
    public ResponseEntity<String> handlePushRequest(HttpServletRequest request)
    {
        if(null == request.getParameter(ClientLogEntry.PARAM_BUTTON_ID)){
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        if(null == request.getParameter(ClientLogEntry.PARAM_EVENT)){
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        String ipAddress = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        ClientLogEntry item = new ClientLogEntry();
        item.setUserAgent(request.getHeader(USER_AGENT_HEADER));
        item.setRemoteAddr(ipAddress);
        item.setTimestamp(new Date());
        item.setReferrer(request.getParameter(ClientLogEntry.PARAM_REFERRER));
        item.setStatCookie(request.getParameter(ClientLogEntry.PARAM_STAT_COOKIE));
        item.setButtonId(request.getParameter(ClientLogEntry.PARAM_BUTTON_ID));
        item.setCallId(request.getParameter(ClientLogEntry.PARAM_CALL_ID));
        item.setType(request.getParameter(ClientLogEntry.PARAM_EVENT));
        item.setDetails(request.getParameter(ClientLogEntry.PARAM_DETAILS));

        service.pushClientLogEntry(item);
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @ResponseBody
    @RequestMapping("/flush")
    public ResponseEntity<String> handleFlushRequest()
    {
        try{
            service.flushClientsLog();
            service.flushCallsLog();
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @ResponseBody
    @RequestMapping("/consolidate")
    public ResponseEntity<String> handleConsolidateRequest(){
        try{
            service.consolidateCalls();
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @ResponseBody
    @RequestMapping(value="/details", method=RequestMethod.GET)
    public ResponseEntity<String> handleDetailsRequestWithoutParameters(HttpServletResponse response){
        SimpleDateFormat sdt = new SimpleDateFormat("yyyyMMdd");
        response.addHeader("Location", "/details/"+sdt.format(new Date()));
        return new ResponseEntity<String>(HttpStatus.MOVED_TEMPORARILY);
    }

    @ResponseBody
    @RequestMapping(value="/details/{date}", method=RequestMethod.GET)
    public ResponseEntity<String> handleDetailsRequest(@PathVariable String date)
    {
        CsvBuilder csv = new CsvBuilder();
        csv.addColumn("date", "getStart");
        csv.addColumn("wait", "getWaitDuration");
        csv.addColumn("talk", "getTalkDuration");
        csv.addColumn("failed", "getFailedDetails");
        csv.addColumn("button", "getExplicitTitle");
        csv.addColumn("callId", "getCallId");
        csv.addColumn("sipId", "getSipId");
        csv.addColumn("a", "getA");
        csv.addColumn("b", "getB");

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date parsedDate =  sdf.parse(date);

            service.flushClientsLog();
            service.flushCallsLog();
            service.consolidateCalls();

            CallConsolidatedEntry.setButtons(service.getButtons());
            List<CallConsolidatedEntry> calls = service.findConsolidatedCalls(parsedDate);
            csv.buildFromList(calls);

            String filename = "a3-stat-"+date+".csv";
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-type", "text/csv");
            responseHeaders.add("Content-Disposition", "attachment; filename=\""+filename+"\"");
            return new ResponseEntity<String>(csv.getResult(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(value="/summary/{buttonId}/{date}", method=RequestMethod.GET)
    public ResponseEntity<String> handleSummaryRequest(@PathVariable String buttonId, @PathVariable String date)
    {
        CsvBuilder csv = new CsvBuilder();
        csv.addColumn("day", "getDayOfMonth");
        csv.addColumn("count", "getCount");
        csv.addColumn("talkSummary", "getSummaryDuration");
        csv.addColumn("talkMean", "getMeanDuration");
        csv.addColumn("waitSummary", "getSummaryWait");
        csv.addColumn("waitMean", "getMeanWait");
        csv.buildFirstLine();

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Date parsedDate =  sdf.parse(date);

            service.flushClientsLog();
            service.flushCallsLog();
            service.consolidateCalls();

            List<CallConsolidatedEntry> calls = service.findConsolidatedCalls(buttonId, parsedDate);
            Map<String, CsvCallsSummaryRow> result = new LinkedHashMap<String, CsvCallsSummaryRow>();

            SimpleDateFormat sdf2 = new SimpleDateFormat("d");
            String dayOfMonth;
            CsvCallsSummaryRow row;

            for(CallConsolidatedEntry cce : calls){
                if(!cce.isStarted()){ continue; }
                dayOfMonth = sdf2.format(cce.getStart());
                if(!result.containsKey(dayOfMonth)){
                    row = new CsvCallsSummaryRow(dayOfMonth);
                    result.put(dayOfMonth, row);
                }else{
                    row = result.get(dayOfMonth);
                }
                row.addCallEntry(cce);
            }

            boolean exg = false;
            for(String key : result.keySet()){
                row = result.get(key);
                if(!exg){
                    csv.explicitGetters(row);
                    exg = true;
                }
                csv.buildEntryLine(row);
            }

            Map<String, String> buttons = service.getButtons();
            String filename = "a3-stat-"+date+"-"+buttons.get(buttonId)+".csv";
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-type", "text/csv");
            responseHeaders.add("Content-Disposition", "attachment; filename=\""+filename+"\"");
            return new ResponseEntity<String>(csv.getResult(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
