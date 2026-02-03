package com.lollito.fm.service;

import com.lollito.fm.model.*;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MatchProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private MatchRepository matchRepository;
    @Autowired private SimulationMatchService simulationMatchService;
    @Autowired private SeasonService seasonService;
    @Autowired private LeagueService leagueService;
    @Autowired private SeasonRepository seasonRepository;

    @Async
    @Transactional
    public void processMatch(Long matchId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null || match.getStatus() != MatchStatus.SCHEDULED) {
            return;
        }

        logger.info("Processing match {} : {} vs {}", match.getId(), match.getHome().getName(), match.getAway().getName());
        match.setStatus(MatchStatus.IN_PROGRESS);
        matchRepository.saveAndFlush(match);

        try {
            simulationMatchService.simulate(match);
            matchRepository.saveAndFlush(match);
            checkRoundAndSeasonProgression(match);
        } catch (Exception e) {
            logger.error("Error processing match " + match.getId(), e);
            match.setStatus(MatchStatus.FAILED);
            matchRepository.save(match);
        }
    }

    private void checkRoundAndSeasonProgression(Match match) {
        Round round = match.getRound();
        Season season = seasonRepository.findByIdWithLock(round.getSeason().getId())
                .orElseThrow(() -> new RuntimeException("Season not found"));

        long unfinishedCount = matchRepository.countByRoundAndFinish(round, Boolean.FALSE);
        if (unfinishedCount == 0) {
            if (season.getNextRoundNumber() > round.getNumber()) {
                // Already advanced
                return;
            }
            logger.info("All matches in round {} finished. Advancing...", round.getNumber());
            season.setNextRoundNumber(round.getNumber() + 1);
            if (round.getLast()) {
                logger.info("Last round of season finished. Creating new season...");
                League league = season.getLeague();
                league.addSeasonHistory(season);
                league.setCurrentSeason(seasonService.create(league, LocalDateTime.now().plusMinutes(10)));
                leagueService.save(league);
            } else {
                seasonRepository.save(season);
            }
        }
    }
}
