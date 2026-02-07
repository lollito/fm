package com.lollito.fm.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Stats;
import com.lollito.fm.model.dto.EventHistoryDTO;
import com.lollito.fm.model.dto.LiveMatchSummaryDTO;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.repository.rest.LiveMatchSessionRepository;
import com.lollito.fm.repository.rest.MatchRepository;

import lombok.Data;

@Service
public class LiveMatchService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private LiveMatchSessionRepository liveMatchSessionRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MatchMapper matchMapper;

    @Autowired
    @Lazy
    private MatchProcessor matchProcessor;

    @Transactional
    public void createSession(Match match) {
        try {
            // Convert to DTO to get all nested objects correctly mapped
            MatchDTO matchDTO = matchMapper.toDto(match);

            LiveMatchSession session = LiveMatchSession.builder()
                    .matchId(match.getId())
                    .startTime(LocalDateTime.now())
                    .currentMinute(0)
                    .homeScore(0)
                    .awayScore(0)
                    .events(objectMapper.writeValueAsString(matchDTO.getEvents()))
                    .stats(objectMapper.writeValueAsString(matchDTO.getStats()))
                    .playerStats(objectMapper.writeValueAsString(matchDTO.getPlayerStats()))
                    .finished(false)
                    .build();

            liveMatchSessionRepository.save(session);
            logger.info("Created LiveMatchSession for match {}", match.getId());
        } catch (Exception e) {
            logger.error("Error creating live match session", e);
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void updateLiveMatches() {
        List<LiveMatchSession> sessions = liveMatchSessionRepository.findByFinishedFalse();
        LocalDateTime now = LocalDateTime.now();
        // 3 minutes real time = 180 seconds
        // 90 minutes game time

        long matchDurationSeconds = 180; // 3 minutes

        for (LiveMatchSession session : sessions) {
            try {
                long secondsElapsed = Duration.between(session.getStartTime(), now).getSeconds();
                int newMinute = (int) ((secondsElapsed * 90) / matchDurationSeconds);

                if (newMinute > 90) newMinute = 90; // Cap at 90 until finish logic runs

                if (newMinute > session.getCurrentMinute()) {
                    processEvents(session, newMinute);
                    session.setCurrentMinute(newMinute);
                    liveMatchSessionRepository.save(session);

                    broadcastUpdate(session);
                }

                if (secondsElapsed >= matchDurationSeconds) {
                    finishMatch(session);
                }
            } catch (Exception e) {
                logger.error("Error updating session {}", session.getId(), e);
            }
        }
    }

    private void processEvents(LiveMatchSession session, int newMinute) throws Exception {
        int oldMinute = session.getCurrentMinute();
        if (session.getEvents() == null) return;
        List<EventHistoryDTO> allEvents = objectMapper.readValue(session.getEvents(), new TypeReference<List<EventHistoryDTO>>(){});

        List<EventHistoryDTO> newEvents = allEvents.stream()
            .filter(e -> e.getMinute() > oldMinute && e.getMinute() <= newMinute)
            .collect(Collectors.toList());

        for (EventHistoryDTO event : newEvents) {
            if (event.getHomeScore() != null) session.setHomeScore(event.getHomeScore());
            if (event.getAwayScore() != null) session.setAwayScore(event.getAwayScore());
            // Broadcast event to /topic/match/{id}/events
            messagingTemplate.convertAndSend("/topic/match/" + session.getMatchId() + "/events", event);
        }
    }

    private void broadcastUpdate(LiveMatchSession session) {
        LiveMatchUpdateDTO dto = new LiveMatchUpdateDTO();
        dto.setMatchId(session.getMatchId());
        dto.setHomeScore(session.getHomeScore());
        dto.setAwayScore(session.getAwayScore());
        dto.setCurrentMinute(session.getCurrentMinute());
        dto.setCurrentPhase(session.getCurrentMinute() >= 90 ? "FINISHED" : (session.getCurrentMinute() >= 45 ? "SECOND_HALF" : "FIRST_HALF"));
        dto.setSpectatorCount(1000); // Mock
        dto.setWeatherConditions("Sunny");
        dto.setIntensity("HIGH");
        dto.setAdditionalTime(0);

        messagingTemplate.convertAndSend("/topic/match/" + session.getMatchId(), dto);
    }

    private void finishMatch(LiveMatchSession session) {
        if (session.getFinished()) return;
        session.setFinished(true);
        session.setCurrentMinute(90); // Ensure it says 90
        liveMatchSessionRepository.save(session);
        matchProcessor.finalizeMatch(session.getMatchId(), session);
        broadcastUpdate(session); // Send final state
    }

    @Transactional(readOnly = true)
    public List<LiveMatchData> getAllLiveMatches() {
        List<LiveMatchSession> sessions = liveMatchSessionRepository.findByFinishedFalse();
        if (sessions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> matchIds = sessions.stream()
                .map(LiveMatchSession::getMatchId)
                .collect(Collectors.toList());

        List<Match> matches = matchRepository.findAllById(matchIds);

        Map<Long, Match> matchMap = matches.stream()
                .collect(Collectors.toMap(Match::getId, m -> m));

        return sessions.stream().map(session -> {
            Match match = matchMap.get(session.getMatchId());
            if (match == null) return null;

            LiveMatchData data = new LiveMatchData();
            data.setMatch(matchMapper.toDto(match));
            data.setHomeScore(session.getHomeScore());
            data.setAwayScore(session.getAwayScore());
            data.setCurrentMinute(session.getCurrentMinute());
            data.setCurrentPhase(session.getCurrentMinute() >= 90 ? "FINISHED" : (session.getCurrentMinute() >= 45 ? "SECOND_HALF" : "FIRST_HALF"));
            return data;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Object getLiveMatchData(Long matchId) {
        LiveMatchSession session = liveMatchSessionRepository.findByMatchId(matchId)
                .orElseThrow(() -> new RuntimeException("Live match not found"));

        Match match = matchRepository.findById(matchId).orElse(null);
        MatchDTO matchDTO = matchMapper.toDto(match);

        try {
            List<EventHistoryDTO> allEvents = objectMapper.readValue(session.getEvents(), new TypeReference<List<EventHistoryDTO>>(){});
            List<EventHistoryDTO> currentEvents = allEvents.stream()
                    .filter(e -> e.getMinute() <= session.getCurrentMinute())
                    .collect(Collectors.toList());

            LiveMatchData data = new LiveMatchData();
            data.setMatch(matchDTO);
            data.setHomeScore(session.getHomeScore());
            data.setAwayScore(session.getAwayScore());
            data.setCurrentMinute(session.getCurrentMinute());
            data.setEvents(currentEvents);
            data.setSpectatorCount(matchDTO.getSpectators());
            data.setWeatherConditions("Sunny");
            data.setIntensity("HIGH");
            data.setCurrentPhase(session.getCurrentMinute() >= 90 ? "FINISHED" : (session.getCurrentMinute() >= 45 ? "SECOND_HALF" : "FIRST_HALF"));
            data.setAdditionalTime(0); // Mock

            return data;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing session data", e);
        }
    }

    @Transactional(readOnly = true)
    public List<LiveMatchSummaryDTO> getAllLiveMatchSummaries() {
        List<LiveMatchSession> sessions = liveMatchSessionRepository.findAll();
        // Sort by start time descending
        sessions.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));

        return sessions.stream().map(session -> {
            Match match = matchRepository.findById(session.getMatchId()).orElse(null);
            String homeName = "Unknown";
            String awayName = "Unknown";
            if (match != null) {
                homeName = match.getHome().getName();
                awayName = match.getAway().getName();
            }
            return LiveMatchSummaryDTO.builder()
                    .sessionId(session.getId())
                    .matchId(session.getMatchId())
                    .homeTeamName(homeName)
                    .awayTeamName(awayName)
                    .homeScore(session.getHomeScore())
                    .awayScore(session.getAwayScore())
                    .currentMinute(session.getCurrentMinute())
                    .finished(session.getFinished())
                    .startTime(session.getStartTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void forceFinish(Long matchId) {
        LiveMatchSession session = liveMatchSessionRepository.findByMatchId(matchId)
                .orElseThrow(() -> new RuntimeException("Live match session not found for match " + matchId));

        finishMatch(session);
    }

    @Transactional
    public void reset(Long matchId) {
        LiveMatchSession session = liveMatchSessionRepository.findByMatchId(matchId).orElse(null);
        if (session != null) {
            liveMatchSessionRepository.delete(session);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setStatus(MatchStatus.SCHEDULED);
        match.setHomeScore(null);
        match.setAwayScore(null);
        match.setFinish(false);
        match.getEvents().clear();
        match.getPlayerStats().clear();
        match.setStats(new Stats());

        matchRepository.save(match);
    }

    @Data
    public static class LiveMatchData {
        private MatchDTO match;
        private Integer homeScore;
        private Integer awayScore;
        private Integer currentMinute;
        private List<EventHistoryDTO> events;
        private Integer spectatorCount;
        private String weatherConditions;
        private String intensity;
        private String currentPhase;
        private Integer additionalTime;
    }

    @Data
    public static class LiveMatchUpdateDTO {
        private Long matchId;
        private Integer homeScore;
        private Integer awayScore;
        private Integer currentMinute;
        private String currentPhase;
        private Integer spectatorCount;
        private String weatherConditions;
        private String intensity;
        private Integer additionalTime;
    }
}
