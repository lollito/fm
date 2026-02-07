package com.lollito.fm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lollito.fm.service.ServerService;

@Component
@Slf4j
public class ScheduledTasks {

	@Autowired private ServerService serverService;
	
    @Scheduled(initialDelayString = "${fm.scheduling.general.initial-delay}", fixedRateString = "${fm.scheduling.general.fixed-rate}")
    public void deleteAllServers() {
        log.info("Starting deleteAllServers...");
	    serverService.deleteAll();
        log.info("Finished deleteAllServers.");
    }
}
