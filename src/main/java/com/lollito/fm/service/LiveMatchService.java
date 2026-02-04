package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.CardType;
import com.lollito.fm.model.EventSeverity;
import com.lollito.fm.model.EventType;
import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchEvent;
import com.lollito.fm.model.MatchIntensity;
import com.lollito.fm.model.MatchPhase;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.dto.LiveMatchUpdateDTO;
import com.lollito.fm.model.dto.MatchEventDTO;
import com.lollito.fm.repository.rest.LiveMatchSessionRepository;
import com.lollito.fm.repository.rest.MatchEventRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.utils.RandomUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class LiveMatchService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LiveMatchSessionRepository liveMatchSessionRepository;

    @Autowired
    private MatchEventRepository matchEventRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    @Lazy
    private LiveMatchService self;

    /**
     * Start live match session
     */
    public LiveMatchSession startLiveMatch(Long matchId) {
        Match match = matchService.findById(matchId);

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            // Allow restarting if needed or handle appropriately
            // throw new IllegalStateException("Match is not scheduled for live viewing");
        }

        LiveMatchSession session = LiveMatchSession.builder()
            .match(match)
            .currentPhase(MatchPhase.PRE_MATCH)
            .currentMinute(0)
            .additionalTime(0)
            .matchStartTime(LocalDateTime.now())
            .homeScore(0)
            .awayScore(0)
            .isPaused(false)
            .spectatorCount(0)
            .intensity(MatchIntensity.MODERATE)
            .weatherConditions(generateWeatherConditions())
            .temperature(generateTemperature())
            .build();

        session = liveMatchSessionRepository.save(session);

        // Update match status
        match.setStatus(MatchStatus.LIVE);
        matchRepository.save(match);

        // Create kick-off event
        createMatchEvent(session, null, null, EventType.KICK_OFF, 0,
                        "Match kicks off!", "The referee blows the whistle to start the match.");

        // Start match simulation in background
        self.startMatchSimulation(session);

        return session;
    }

    /**
     * Simulate match events in real-time
     */
    @Async
    @Transactional
    public void startMatchSimulation(LiveMatchSession session) {
        try {
            simulateMatchPhase(session, MatchPhase.FIRST_HALF);

            // Half time
            session.setCurrentPhase(MatchPhase.HALF_TIME);
            session.setHalfTimeStart(LocalDateTime.now());
            createMatchEvent(session, null, null, EventType.HALF_TIME, 45,
                           "Half Time", "The referee signals the end of the first half.");
            broadcastMatchUpdate(session);

            // Wait for half time (compressed time)
            Thread.sleep(10000); // Reduced to 10 seconds for demo/testing

            // Second half
            session.setCurrentPhase(MatchPhase.SECOND_HALF);
            session.setSecondHalfStart(LocalDateTime.now());
            session.setCurrentMinute(45);
            simulateMatchPhase(session, MatchPhase.SECOND_HALF);

            // Full time
            session.setCurrentPhase(MatchPhase.FINISHED);
            session.setMatchEndTime(LocalDateTime.now());
            createMatchEvent(session, null, null, EventType.FULL_TIME, 90,
                           "Full Time", "The referee blows the final whistle.");

            // Finalize match
            finalizeMatch(session);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Match simulation interrupted", e);
        }
    }

    /**
     * Simulate events for a match phase
     */
    private void simulateMatchPhase(LiveMatchSession session, MatchPhase phase) throws InterruptedException {
        int phaseDuration = phase.getDuration();
        int startMinute = session.getCurrentMinute();

        for (int minute = startMinute; minute < startMinute + phaseDuration; minute++) {
            session.setCurrentMinute(minute);

            // Simulate events for this minute
            simulateMinuteEvents(session, minute);

            // Broadcast minute update
            if (minute % 5 == 0) { // Every 5 minutes
                broadcastMatchUpdate(session);
            }

            // Wait for next minute (compressed time: 1 second = 1 minute)
            Thread.sleep(1000);
        }

        // Add additional time
        int additionalTime = calculateAdditionalTime(session, phase);
        session.setAdditionalTime(additionalTime);

        for (int i = 1; i <= additionalTime; i++) {
            session.setCurrentMinute(startMinute + phaseDuration);
            session.setAdditionalTime(i);

            simulateMinuteEvents(session, startMinute + phaseDuration);
            broadcastMatchUpdate(session);

            Thread.sleep(1000);
        }
    }

    /**
     * Simulate events within a minute
     */
    private void simulateMinuteEvents(LiveMatchSession session, int minute) {
        Match match = session.getMatch();
        Team homeTeam = match.getHome().getTeam(); // Changed to getHome().getTeam()
        Team awayTeam = match.getAway().getTeam(); // Changed to getAway().getTeam()

        // Calculate event probabilities based on team strengths, current score, etc.
        double eventProbability = calculateEventProbability(session, minute);

        if (RandomUtils.randomValue(0.0, 1.0) < eventProbability) {
            EventType eventType = selectRandomEventType(session);
            Team eventTeam = selectEventTeam(homeTeam, awayTeam, session);
            Player eventPlayer = selectEventPlayer(eventTeam, eventType);

            processMatchEvent(session, eventTeam, eventPlayer, eventType, minute);
        }

        // Update match intensity based on events and time
        updateMatchIntensity(session, minute);
    }

    /**
     * Process a match event
     */
    private void processMatchEvent(LiveMatchSession session, Team team, Player player,
                                 EventType eventType, int minute) {
        String description = generateEventDescription(eventType, player, team, session);
        String detailedDescription = generateDetailedEventDescription(eventType, player, team, session);

        MatchEvent event = createMatchEvent(session, team, player, eventType, minute,
                                          description, detailedDescription);

        // Process event effects
        switch (eventType) {
            case GOAL: processGoalEvent(session, event, team); break;
            case YELLOW_CARD: processCardEvent(session, event, CardType.YELLOW); break;
            case RED_CARD: processCardEvent(session, event, CardType.RED); break;
            case SUBSTITUTION: processSubstitutionEvent(session, event, team); break;
            case INJURY: processInjuryEvent(session, event, player); break;
            case PENALTY: processPenaltyEvent(session, event, team); break;
            default: break;
        }

        // Broadcast event to live viewers
        broadcastMatchEvent(event);

        // Update match statistics
        updateMatchStatistics(session, event);
    }

    /**
     * Process goal event
     */
    public void processGoalEvent(LiveMatchSession session, MatchEvent event, Team scoringTeam) {
        if (scoringTeam.equals(session.getMatch().getHome().getTeam())) {
            session.setHomeScore(session.getHomeScore() + 1);
        } else {
            session.setAwayScore(session.getAwayScore() + 1);
        }

        event.setHomeScore(session.getHomeScore());
        event.setAwayScore(session.getAwayScore());
        matchEventRepository.save(event);

        // Check for assist
        if (RandomUtils.randomValue(0.0, 1.0) < 0.6) { // 60% chance of assist
            Player assistPlayer = selectAssistPlayer(scoringTeam, event.getPlayer());
            if (assistPlayer != null) {
                event.setAssistPlayer(assistPlayer);
                matchEventRepository.save(event);

                // Create assist event
                createMatchEvent(session, scoringTeam, assistPlayer, EventType.ASSIST,
                               event.getMinute(),
                               assistPlayer.getName() + " " + assistPlayer.getSurname() + " assists!",
                               "Great pass sets up the goal.");
            }
        }

        // Increase match intensity after goal
        session.setIntensity(MatchIntensity.HIGH);

        liveMatchSessionRepository.save(session);
    }

    private void processCardEvent(LiveMatchSession session, MatchEvent event, CardType type) {
        event.setCardType(type);
        matchEventRepository.save(event);
    }

    private void processSubstitutionEvent(LiveMatchSession session, MatchEvent event, Team team) {
        // Simple logic for now
    }

    private void processInjuryEvent(LiveMatchSession session, MatchEvent event, Player player) {
        // Simple logic
    }

    private void processPenaltyEvent(LiveMatchSession session, MatchEvent event, Team team) {
        // Logic to decide if goal or save
        if(RandomUtils.randomValue(0.0, 1.0) < 0.75) {
            // Goal
            createMatchEvent(session, team, event.getPlayer(), EventType.GOAL, event.getMinute(), "Penalty Goal!", "Scores from the spot.");
            processGoalEvent(session, event, team);
        } else {
            // Save
            createMatchEvent(session, team == session.getMatch().getHome().getTeam() ? session.getMatch().getAway().getTeam() : session.getMatch().getHome().getTeam(), null, EventType.SAVE, event.getMinute(), "Penalty Saved!", "Keeper denies the penalty.");
        }
    }

    /**
     * Create match event
     */
    public MatchEvent createMatchEvent(LiveMatchSession session, Team team, Player player,
                                      EventType eventType, int minute, String description,
                                      String detailedDescription) {
        MatchEvent event = MatchEvent.builder()
            .match(session.getMatch())
            .session(session)
            .team(team)
            .player(player)
            .eventType(eventType)
            .minute(minute)
            .additionalTime(session.getAdditionalTime())
            .description(description)
            .detailedDescription(detailedDescription)
            .homeScore(session.getHomeScore())
            .awayScore(session.getAwayScore())
            .severity(eventType.getDefaultSeverity())
            .eventTime(LocalDateTime.now())
            .isKeyEvent(isKeyEvent(eventType))
            .build();

        event = matchEventRepository.save(event);
        // session.getEvents().add(event); // Removed because mappedBy="session" handles ownership via session_id in MatchEvent, but we might want to keep the in-memory list updated if session is kept alive.
        // However, fetching session again will retrieve events.

        return event;
    }

    /**
     * Broadcast match update to WebSocket subscribers
     */
    private void broadcastMatchUpdate(LiveMatchSession session) {
        LiveMatchUpdateDTO update = LiveMatchUpdateDTO.builder()
            .matchId(session.getMatch().getId())
            .currentPhase(session.getCurrentPhase())
            .currentMinute(session.getCurrentMinute())
            .additionalTime(session.getAdditionalTime())
            .homeScore(session.getHomeScore())
            .awayScore(session.getAwayScore())
            .intensity(session.getIntensity())
            .spectatorCount(session.getSpectatorCount())
            .build();

        messagingTemplate.convertAndSend("/topic/match/" + session.getMatch().getId(), update);
    }

    /**
     * Broadcast match event to WebSocket subscribers
     */
    private void broadcastMatchEvent(MatchEvent event) {
        MatchEventDTO eventDTO = convertToDTO(event);
        messagingTemplate.convertAndSend("/topic/match/" + event.getMatch().getId() + "/events", eventDTO);
    }

    /**
     * Get live match session
     */
    public LiveMatchSession getLiveMatchSession(Long matchId) {
        return liveMatchSessionRepository.findByMatchId(matchId)
            .orElseThrow(() -> new EntityNotFoundException("Live match session not found"));
    }

    /**
     * Join live match as spectator
     */
    public void joinLiveMatch(Long matchId, String userId) {
        LiveMatchSession session = getLiveMatchSession(matchId);
        if(session.getSpectatorCount() == null) session.setSpectatorCount(0);
        session.setSpectatorCount(session.getSpectatorCount() + 1);
        liveMatchSessionRepository.save(session);

        // Send current match state to new viewer
        broadcastMatchUpdate(session);

        // Send recent events
        List<MatchEvent> recentEvents = matchEventRepository.findByMatchId(matchId).stream()
            .filter(event -> event.getEventTime() != null && event.getEventTime().isAfter(LocalDateTime.now().minusMinutes(5)))
            .collect(Collectors.toList());

        for (MatchEvent event : recentEvents) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/match-events",
                                                 convertToDTO(event));
        }
    }

    /**
     * Leave live match
     */
    public void leaveLiveMatch(Long matchId) {
        LiveMatchSession session = getLiveMatchSession(matchId);
        if(session.getSpectatorCount() == null) session.setSpectatorCount(0);
        session.setSpectatorCount(Math.max(0, session.getSpectatorCount() - 1));
        liveMatchSessionRepository.save(session);
    }

    private double calculateEventProbability(LiveMatchSession session, int minute) {
        double baseProbability = 0.15; // 15% chance per minute

        // Increase probability in final minutes
        if (minute > 80) baseProbability *= 1.5;
        if (minute > 85) baseProbability *= 1.3;

        // Adjust based on match intensity
        if (session.getIntensity() != null) {
            switch (session.getIntensity()) {
                case LOW: baseProbability *= 0.7; break;
                case HIGH: baseProbability *= 1.3; break;
                case EXTREME: baseProbability *= 1.6; break;
                default: break;
            }
        }

        return Math.min(0.8, baseProbability); // Cap at 80%
    }

    private EventType selectRandomEventType(LiveMatchSession session) {
        // Weighted random selection based on match context
        Map<EventType, Double> eventWeights = Map.of(
            EventType.SHOT_ON_TARGET, 0.25,
            EventType.SHOT_OFF_TARGET, 0.20,
            EventType.FOUL, 0.15,
            EventType.CORNER, 0.10,
            EventType.GOAL, 0.08,
            EventType.YELLOW_CARD, 0.07,
            EventType.FREE_KICK, 0.06,
            EventType.OFFSIDE, 0.05,
            EventType.SAVE, 0.03
            // EventType.RED_CARD, 0.01 // Map.of doesn't support 11 entries conveniently if I add red card
        );
        // Using a mutable map or explicit Map.ofEntries if needed, but for now 10 entries.
        // I'll stick to 10 common events for simplicity or use Map.ofEntries for more.
        return RandomUtils.weightedRandomSelection(eventWeights);
    }

    private String generateEventDescription(EventType eventType, Player player, Team team, LiveMatchSession session) {
        String playerName = player != null ? player.getName() + " " + player.getSurname() : "Player";
        String teamName = "Team";
        if (team != null && session != null && session.getMatch() != null) {
		if (team.equals(session.getMatch().getHome().getTeam())) {
			teamName = session.getMatch().getHome().getName();
		} else if (team.equals(session.getMatch().getAway().getTeam())) {
			teamName = session.getMatch().getAway().getName();
		}
        }

        switch (eventType) {
            case GOAL: return playerName + " scores for " + teamName + "!";
            case YELLOW_CARD: return playerName + " receives a yellow card";
            case RED_CARD: return playerName + " is sent off!";
            case SUBSTITUTION: return "Substitution for " + teamName;
            case SHOT_ON_TARGET: return playerName + " shoots on target";
            case SHOT_OFF_TARGET: return playerName + " shoots wide";
            case SAVE: return "Great save by the goalkeeper!";
            case CORNER: return "Corner kick for " + teamName;
            case FREE_KICK: return "Free kick awarded to " + teamName;
            case FOUL: return "Foul by " + playerName;
            case OFFSIDE: return playerName + " caught offside";
            default: return eventType.getDisplayName();
        }
    }

    private String generateWeatherConditions() {
        List<String> conditions = List.of("Sunny", "Cloudy", "Rainy", "Windy");
        return RandomUtils.randomValueFromList(conditions);
    }

    private Double generateTemperature() {
        return RandomUtils.randomValue(5.0, 30.0);
    }

    private int calculateAdditionalTime(LiveMatchSession session, MatchPhase phase) {
        return RandomUtils.randomValue(1, 5);
    }

    private void updateMatchIntensity(LiveMatchSession session, int minute) {
         if(Math.abs(session.getHomeScore() - session.getAwayScore()) <= 1) {
             session.setIntensity(MatchIntensity.HIGH);
         } else {
             session.setIntensity(MatchIntensity.MODERATE);
         }
    }

    private void finalizeMatch(LiveMatchSession session) {
         Match match = session.getMatch();
         match.setFinish(true);
         match.setHomeScore(session.getHomeScore());
         match.setAwayScore(session.getAwayScore());
         match.setStatus(MatchStatus.COMPLETED);
         matchRepository.save(match);
    }

    private void updateMatchStatistics(LiveMatchSession session, MatchEvent event) {
        // Placeholder
    }

    private Team selectEventTeam(Team home, Team away, LiveMatchSession session) {
        return RandomUtils.randomValue(0, 1) == 0 ? home : away;
    }

    private Player selectEventPlayer(Team team, EventType type) {
        if(team.getPlayers() == null || team.getPlayers().isEmpty()) return null;
        return RandomUtils.randomValueFromList(team.getPlayers());
    }

    private Player selectAssistPlayer(Team team, Player scorer) {
         if(team.getPlayers() == null) return null;
         List<Player> players = new ArrayList<>(team.getPlayers());
         players.remove(scorer);
         if(players.isEmpty()) return null;
         return RandomUtils.randomValueFromList(players);
    }

    private String generateDetailedEventDescription(EventType type, Player player, Team team, LiveMatchSession session) {
        return generateEventDescription(type, player, team, session);
    }

    private boolean isKeyEvent(EventType type) {
        return type == EventType.GOAL || type == EventType.RED_CARD || type == EventType.PENALTY;
    }

    public List<LiveMatchSession> getActiveLiveMatches() {
        return liveMatchSessionRepository.findByCurrentPhaseNot(MatchPhase.FINISHED);
    }

    public List<MatchEvent> getMatchEvents(Long matchId, Integer fromMinute) {
        if(fromMinute == null) {
            return matchEventRepository.findByMatchId(matchId);
        } else {
            return matchEventRepository.findByMatchIdAndMinuteGreaterThanEqual(matchId, fromMinute);
        }
    }

    public MatchEventDTO convertToDTO(MatchEvent event) {
        String teamName = null;
        if (event.getTeam() != null && event.getMatch() != null) {
		if (event.getTeam().equals(event.getMatch().getHome().getTeam())) {
			teamName = event.getMatch().getHome().getName();
		} else if (event.getTeam().equals(event.getMatch().getAway().getTeam())) {
			teamName = event.getMatch().getAway().getName();
		}
        }

        return MatchEventDTO.builder()
            .id(event.getId())
            .matchId(event.getMatch().getId())
            .eventType(event.getEventType())
            .minute(event.getMinute())
            .additionalTime(event.getAdditionalTime())
            .description(event.getDescription())
            .detailedDescription(event.getDetailedDescription())
            .playerName(event.getPlayer() != null ?
                       event.getPlayer().getName() + " " + event.getPlayer().getSurname() : null)
            .teamName(teamName)
            .homeScore(event.getHomeScore())
            .awayScore(event.getAwayScore())
            .severity(event.getSeverity())
            .isKeyEvent(event.getIsKeyEvent())
            .build();
    }
}
