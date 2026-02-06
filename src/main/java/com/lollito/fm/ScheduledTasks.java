package com.lollito.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lollito.fm.service.ServerService;

@Component
public class ScheduledTasks {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ServerService serverService;
	
    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteAllServers() {
	logger.info("Deleting all servers");
	serverService.deleteAll();
    }
}
