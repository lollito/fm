package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.repository.rest.MatchRepository;

@Service
public class MatchSchedulerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private MatchRepository matchRepository;
    @Autowired private MatchProcessor matchProcessor;

    @Scheduled(cron = "${fm.match.processing.cron:0 * * * * *}")
    public void processScheduledMatches() {
        LocalDateTime now = LocalDateTime.now();
        List<Match> matchesToRun = matchRepository.findByStatusAndDateBefore(MatchStatus.SCHEDULED, now);

        if (!matchesToRun.isEmpty()) {
            logger.info("Found {} matches to run.", matchesToRun.size());
            for (Match match : matchesToRun) {
                matchProcessor.processMatch(match.getId());
            }
        }
    }
}
