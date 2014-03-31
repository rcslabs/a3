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
import java.util.List;


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
    public ResponseEntity<String> handleDetailsRequest(@PathVariable String date){
        String response = null;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date parsedDate =  sdf.parse(date);

            service.flushClientsLog();
            service.flushCallsLog();
            service.consolidateCalls();

            List<CallConsolidatedEntry> calls = service.findConsolidatedCalls(parsedDate);
            CsvBuilder<CallConsolidatedEntry> csv = new CsvBuilder<CallConsolidatedEntry>();
            csv.addProperty("date", "getStart");
            csv.addProperty("started", "isStarted");
            csv.addProperty("wait", "getWaitDuration");
            csv.addProperty("talk", "getTalkDuration");
            csv.addProperty("buttonId", "getButtonId");
            csv.addProperty("callId", "getCallId");
            csv.addProperty("sipId", "getSipId");
            csv.addProperty("a", "getA");
            csv.addProperty("b", "getB");
            csv.buildFromList(calls);
            response = csv.getResult();

        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-type", "text/csv");
        responseHeaders.add("Content-Disposition", "attachment; filename=\"stat-"+date+".csv\"");
        return new ResponseEntity<String>(response, responseHeaders, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value="/summary/{date}/{buttonId}", method=RequestMethod.GET)
    public ResponseEntity<String> handleSummaryRequest(@PathVariable String date, @PathVariable String buttonId){
        String response = null;

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            Date parsedDate =  sdf.parse(date);

            service.flushClientsLog();
            service.flushCallsLog();
            service.consolidateCalls();


        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-type", "text/csv");
        responseHeaders.add("Content-Disposition", "attachment; filename=\"stat-"+date+"-"+buttonId+".csv\"");
        return new ResponseEntity<String>(response, responseHeaders, HttpStatus.OK);
    }
}
