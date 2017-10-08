package com.lollito.fm;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {


    @Scheduled(cron = "0 0 1 * * ?")
    public void reportCurrentTime() {
        //TODO delete all game
    }
}