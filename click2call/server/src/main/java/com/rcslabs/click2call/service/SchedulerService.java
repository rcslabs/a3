package com.rcslabs.click2call.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by sx on 13.05.14.
 */
public class SchedulerService {

    @Autowired
    private StatService service;

    @Scheduled(cron = "0 0 4 * * ?")
    public void consolidateStat(){
        service.flushClientsLog();
        service.flushCallsLog();
        service.consolidateCalls();
        service.parseRed5Calls("/var/log/alena/red5calls/");
    }
}
