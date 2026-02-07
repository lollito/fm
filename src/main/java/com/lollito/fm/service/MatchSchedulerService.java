package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.repository.rest.MatchRepository;

@Service
@Slf4j
public class MatchSchedulerService {

    @Autowired private MatchRepository matchRepository;
    @Autowired private MatchProcessor matchProcessor;

    @Scheduled(initialDelayString = "${fm.scheduling.match-processing.initial-delay}", fixedRateString = "${fm.scheduling.match-processing.fixed-rate}")
    public void processScheduledMatches() {
        log.info("Starting processScheduledMatches...");
        LocalDateTime now = LocalDateTime.now();
        List<Match> matchesToRun = matchRepository.findByStatusAndDateBefore(MatchStatus.SCHEDULED, now);

        if (!matchesToRun.isEmpty()) {
            log.info("Found {} matches to run.", matchesToRun.size());
            for (Match match : matchesToRun) {
                matchProcessor.processMatch(match.getId());
            }
        }
        log.info("Finished processScheduledMatches.");
    }
}
