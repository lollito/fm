package com.lollito.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lollito.fm.service.GameService;

@Component
public class ScheduledTasks {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private GameService gameService;
	
    @Scheduled(cron = "0 0/1 * * * ?")
    public void reportCurrentTime() {
    	logger.info("OOOEEEE");
    	gameService.next();
    }
}