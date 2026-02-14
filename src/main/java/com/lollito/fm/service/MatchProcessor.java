package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.event.MatchFinishedEvent;
import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.mapper.MatchPlayerStatsMapper;
import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.League;
import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchPlayerStats;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Stats;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.EventHistoryDTO;
import com.lollito.fm.model.dto.MatchPlayerStatsDTO;
import com.lollito.fm.model.dto.StatsDTO;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class MatchProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private MatchRepository matchRepository;
    @Autowired private SimulationMatchService simulationMatchService;
    @Autowired private SeasonService seasonService;
    @Autowired private LeagueService leagueService;
    @Autowired private SeasonRepository seasonRepository;
    @Autowired private LiveMatchService liveMatchService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MatchMapper matchMapper;
    @Autowired private MatchPlayerStatsMapper matchPlayerStatsMapper;
    @Autowired private PlayerService playerService;
    @Autowired private PlayerHistoryService playerHistoryService;
    @Autowired private RankingService rankingService;
    @Autowired private ApplicationEventPublisher eventPublisher;

    @Async
    @Transactional
    public void processMatch(Long matchId) {
        Match match = matchRepository.findByIdWithSimulationData(matchId).orElse(null);
        if (match == null || match.getStatus() != MatchStatus.SCHEDULED) {
            return;
        }

        logger.info("Processing match {} : {} vs {}", match.getId(), match.getHome().getName(), match.getAway().getName());

        // Simulate to get result
        simulationMatchService.simulate(match, null, false, false);

        try {
            // Create session with the result
            liveMatchService.createSession(match);

            // Reset match to started state (scores 0-0)
            match.setHomeScore(0);
            match.setAwayScore(0);
            match.getEvents().clear();
            match.setStats(new Stats());
            match.getPlayerStats().clear();
            match.setFinish(false);
            match.setStatus(MatchStatus.IN_PROGRESS);

            matchRepository.saveAndFlush(match);

            // Notify users
            notifyUser(match.getHome().getUser(), match.getId(), "MATCH_STARTED", "Match Started!");
            notifyUser(match.getAway().getUser(), match.getId(), "MATCH_STARTED", "Match Started!");
        } catch (Exception e) {
            logger.error("Failed to create live session for match {}. Saving as COMPLETED.", match.getId(), e);

            match.setFinish(true);
            match.setStatus(MatchStatus.COMPLETED);

            rankingService.update(match);
            checkRoundAndSeasonProgression(match);

            matchRepository.saveAndFlush(match);

            notifyUser(match.getHome().getUser(), match.getId(), "MATCH_ENDED", "Match Ended (Simulated)!");
            notifyUser(match.getAway().getUser(), match.getId(), "MATCH_ENDED", "Match Ended (Simulated)!");
        }
    }

    private void notifyUser(User user, Long matchId, String type, String message) {
        if (user != null) {
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/notifications",
                new NotificationDTO(type, matchId, message));
        }
    }

    @Transactional
    public void finalizeMatch(Long matchId, LiveMatchSession session) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));

        try {
            // Restore final state from session
            match.setHomeScore(session.getHomeScore());
            match.setAwayScore(session.getAwayScore());

            // Restore Events
            List<EventHistoryDTO> eventDTOs = objectMapper.readValue(session.getEvents(), new TypeReference<List<EventHistoryDTO>>(){});
            List<EventHistory> events = eventDTOs.stream().map(matchMapper::toEntity).collect(Collectors.toList());

            match.getEvents().clear();
            events.forEach(e -> {
                e.setId(null);
                e.setMatch(match);
            });
            match.getEvents().addAll(events);

            // Restore Stats
            StatsDTO statsDTO = objectMapper.readValue(session.getStats(), StatsDTO.class);
            Stats stats = matchMapper.toEntity(statsDTO);
            stats.setId(null);
            match.setStats(stats);

            // Restore PlayerStats (if persisted in Session)
            if (session.getPlayerStats() != null) {
                List<MatchPlayerStatsDTO> playerStatsDTOs = objectMapper.readValue(session.getPlayerStats(), new TypeReference<List<MatchPlayerStatsDTO>>(){});
                List<MatchPlayerStats> playerStats = playerStatsDTOs.stream()
                        .map(matchPlayerStatsMapper::toEntity)
                        .collect(Collectors.toList());
                playerStats.forEach(mps -> {
                    mps.setMatch(match);
                    mps.setId(null);
                });
                match.getPlayerStats().clear();
                match.getPlayerStats().addAll(playerStats);
            }

            match.setFinish(true);
            match.setStatus(MatchStatus.COMPLETED);

            // Update historical stats (PlayerHistoryService) - was done in simulationMatchService initially
            // But since I discarded the match object, I need to redo it here using the restored stats?
            // `simulationMatchService` called:
            // `match.getPlayerStats().forEach(stats -> playerHistoryService.updateMatchStatistics(stats.getPlayer(), stats));`
            // If I restore `match.playerStats`, I should call this again?
            // Or did `simulationMatchService` execute it before I reset the match?
            // `simulationMatchService` modifies the passed `match` object.
            // But `playerHistoryService.updateMatchStatistics` updates `PlayerHistory` entities (DB).
            // If those updates were flushed, they are saved.
            // `processMatch` is `@Transactional`.
            // If I reset `match` and save it, does it rollback `PlayerHistory` changes?
            // No, `PlayerHistory` are separate entities.
            // BUT: `MatchProcessor.processMatch` is one transaction.
            // If I call `simulationMatchService.simulate`, it saves players and updates history.
            // Then I reset `match`.
            // Then I save `match`.
            // Transaction commits.
            // So `PlayerHistory` updates ARE persisted.
            // And Player attributes (stamina) ARE persisted.
            // So I DON'T need to re-run `playerHistoryService` updates.
            // I ONLY need to restore `Match.playerStats` so the match report is correct.

            rankingService.update(match);
            checkRoundAndSeasonProgression(match);

            eventPublisher.publishEvent(new MatchFinishedEvent(this, match));

            // Notify users match ended
            notifyUser(match.getHome().getUser(), match.getId(), "MATCH_ENDED", "Match Ended!");
            notifyUser(match.getAway().getUser(), match.getId(), "MATCH_ENDED", "Match Ended!");

            // Cleanup session?
            // liveMatchService.deleteSession(session); // optional

        } catch (Exception e) {
            logger.error("Error finalizing match " + matchId, e);
        }
    }

    private void checkRoundAndSeasonProgression(Match match) {
        Round round = match.getRound();
        Season season = seasonRepository.findByIdWithLock(round.getSeason().getId())
                .orElseThrow(() -> new RuntimeException("Season not found"));

        long unfinishedCount = matchRepository.countByRoundAndFinish(round, Boolean.FALSE);
        if (unfinishedCount == 0) {
            if (season.getNextRoundNumber() > round.getNumber()) {
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationDTO {
        private String type;
        private Long matchId;
        private String message;
    }
}
