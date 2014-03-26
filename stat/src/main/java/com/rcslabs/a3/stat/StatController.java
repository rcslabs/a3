package com.rcslabs.a3.stat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


@Controller()
@RequestMapping("/stat")
public class StatController {
    private static final String USER_AGENT_HEADER = "User-Agent";

    @Autowired
    private StatService service;

    @ResponseBody
    @RequestMapping("/push")
    public ResponseEntity<String> handleRequest(HttpServletRequest request)
    {
        if(null == request.getParameter(ClientLogEntry.PARAM_BUTTON_ID)){
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        if(null == request.getParameter(ClientLogEntry.PARAM_EVENT)){
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        ClientLogEntry item = new ClientLogEntry();
        item.setUserAgent(request.getHeader(USER_AGENT_HEADER));
        item.setRemoteAddr(request.getRemoteAddr());
        item.setServerDate(new Date());
        item.setReferrer(request.getParameter(ClientLogEntry.PARAM_REFERRER));
        item.setClientDate(new Date(Long.parseLong(request.getParameter(ClientLogEntry.PARAM_TIMESTAMP), 10)));
        item.setStatCookie(request.getParameter(ClientLogEntry.PARAM_STAT_COOKIE));
        item.setButtonId(request.getParameter(ClientLogEntry.PARAM_BUTTON_ID));
        item.setCallId(request.getParameter(ClientLogEntry.PARAM_CALL_ID));
        item.setEvent(request.getParameter(ClientLogEntry.PARAM_EVENT));
        item.setDetails(request.getParameter(ClientLogEntry.PARAM_DETAILS));

        service.pushClientLogEntry(item);
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }
}
