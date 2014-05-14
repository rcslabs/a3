package com.rcslabs.click2call.controller;

import com.rcslabs.click2call.csv.CsvBuilder;
import com.rcslabs.click2call.csv.CsvCallsSummaryRow;
import com.rcslabs.click2call.csv.CsvCallsSummaryRowTotal;
import com.rcslabs.click2call.entity.CallConsolidatedEntry;
import com.rcslabs.click2call.entity.ClientLogEntry;
import com.rcslabs.click2call.service.ButtonService;
import com.rcslabs.click2call.service.StatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller()
@RequestMapping("/stat")
public class StatController {

    private static final Logger log = LoggerFactory.getLogger(StatController.class);

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    @Autowired
    private StatService service;

    @Autowired
    private ButtonService buttonService;

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

        if(item.getType().equals("LOAD")){
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/stat/img/0.png");
            return new ResponseEntity<String>(headers, HttpStatus.FOUND);
        }else{
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value="/", method=RequestMethod.GET)
    public String handleDefaultRequest() {
        service.flushClientsLog();
        service.flushCallsLog();
        service.consolidateCalls();
        return "index";
    }

    @RequestMapping(value="/details/{date}", method=RequestMethod.GET)
    public @ResponseBody
    CsvBuilder handleDetailsRequest(@PathVariable String date)
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
            CallConsolidatedEntry.setButtons(buttonService.getButtonsTitle());
            List<CallConsolidatedEntry> calls = service.findConsolidatedCalls(parsedDate);
            csv.buildFromList(calls);

            csv.setFilename("a3-stat-"+date+".csv");
            return csv;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @RequestMapping(value="/summary/{buttonId}/{date}", method=RequestMethod.GET)
    public @ResponseBody CsvBuilder handleSummaryRequest(@PathVariable String buttonId, @PathVariable String date)
    {
        CsvBuilder csv = new CsvBuilder();
        csv.addColumn("day", "getDayOfMonth");
        csv.addColumn("count", "getCount");
        csv.addColumn("talkSummary", "getSummaryDuration");
        csv.addColumn("talkMean", "getMeanDuration");
        csv.addColumn("waitSummary", "getSummaryWait");
        csv.addColumn("waitMean", "getMeanWait");
        csv.addColumn("callbackForm", "getCallbackFormCount");
        csv.buildFirstLine();

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Date parsedDate =  sdf.parse(date);
            List<CallConsolidatedEntry> calls = service.findConsolidatedCalls(buttonId, parsedDate);

            // day by day rows in selected month
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

            // merge clients SUBMIT_FORM with day-by-day rows with summary calls
            Map<String, Integer> submitForm = service.findClientSubmitFormEntries(buttonId, parsedDate);
            for(String d : submitForm.keySet()){
                if(!result.containsKey(d)){
                    result.put(d, new CsvCallsSummaryRow(d));
                }
                result.get(d).setCallbackFormCount( submitForm.get(d) );
            }

            // sort by day and calculate total on result
            List<CsvCallsSummaryRow> sorted = new ArrayList<>(result.values());
            Collections.sort(sorted, new Comparator<CsvCallsSummaryRow>() {
                public int compare(CsvCallsSummaryRow o1, CsvCallsSummaryRow o2) {
                    if (Integer.parseInt(o1.getDayOfMonth())<Integer.parseInt(o2.getDayOfMonth())) {
                        return -1;
                    } else if (Integer.parseInt(o1.getDayOfMonth())>Integer.parseInt(o2.getDayOfMonth())) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            CsvCallsSummaryRowTotal totalRow = new CsvCallsSummaryRowTotal();
            boolean exg = false;

            for(CsvCallsSummaryRow row2 : sorted){
                if(!exg){
                    csv.explicitGetters(row2);
                    exg = true;
                }
                csv.buildEntryLine(row2);
                totalRow.addCsvCallsSummaryRow(row2);
            }

            csv.buildEntryLine(totalRow);

            Map<String, String> buttons = buttonService.getButtonsTitle();
            csv.setFilename("a3-stat-"+date+"-"+buttons.get(buttonId)+".csv");
            return csv;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }


    @RequestMapping(value="/count/{date}", method=RequestMethod.GET)
    public @ResponseBody Map handleCountRequest(@PathVariable String date){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Date parsedDate =  sdf.parse(date);
            Map<String, BigInteger> result = service.countCallsByMonth(parsedDate);
            return result;
        } catch (Exception e){
            log.error(e.getMessage(), e);
            return new HashMap();
        }
    }

    @RequestMapping(value="/buttons", method=RequestMethod.GET)
    public @ResponseBody Map handleButtonsRequest(){
        try {
            return  buttonService.getButtonsTitle();
        } catch (Exception e){
            log.error(e.getMessage(), e);
            return new HashMap();
        }
    }
}
