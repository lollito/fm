# Live Match Viewer Implementation

## Overview
Implement a real-time match viewer that displays match events as they happen, providing an immersive experience for users watching their team's matches.

## Technical Requirements

### Database Schema Changes

#### New Entity: MatchEvent
```java
@Entity
@Table(name = "match_event")
public class MatchEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    private Integer minute;
    private Integer additionalTime; // Extra time in the half
    
    private String description;
    private String detailedDescription;
    
    // Event-specific data
    private Integer homeScore; // Score after this event
    private Integer awayScore;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assist_player_id")
    private Player assistPlayer; // For goals
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_in_id")
    private Player substituteIn; // For substitutions
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_out_id")
    private Player substituteOut;
    
    @Enumerated(EnumType.STRING)
    private CardType cardType; // For cards
    
    @Enumerated(EnumType.STRING)
    private EventSeverity severity; // MINOR, NORMAL, MAJOR, CRITICAL
    
    private LocalDateTime eventTime; // Real-time when event occurred
    private Boolean isKeyEvent; // Important events for highlights
    
    private String eventData; // JSON for additional event data
}
```

#### New Entity: LiveMatchSession
```java
@Entity
@Table(name = "live_match_session")
public class LiveMatchSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;
    
    @Enumerated(EnumType.STRING)
    private MatchPhase currentPhase; // PRE_MATCH, FIRST_HALF, HALF_TIME, SECOND_HALF, EXTRA_TIME, PENALTIES, FINISHED
    
    private Integer currentMinute;
    private Integer additionalTime;
    
    private LocalDateTime matchStartTime;
    private LocalDateTime halfTimeStart;
    private LocalDateTime secondHalfStart;
    private LocalDateTime matchEndTime;
    
    private Boolean isPaused;
    private String pauseReason;
    
    private Integer homeScore;
    private Integer awayScore;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("minute ASC, eventTime ASC")
    private List<MatchEvent> events = new ArrayList<>();
    
    private Integer spectatorCount; // Live viewers
    private String weatherConditions;
    private Double temperature;
    
    @Enumerated(EnumType.STRING)
    private MatchIntensity intensity; // LOW, MODERATE, HIGH, EXTREME
}
```

#### Enums to Create
```java
public enum EventType {
    GOAL("Goal", "⚽", EventSeverity.MAJOR),
    ASSIST("Assist", "🅰️", EventSeverity.NORMAL),
    YELLOW_CARD("Yellow Card", "🟨", EventSeverity.NORMAL),
    RED_CARD("Red Card", "🟥", EventSeverity.MAJOR),
    SUBSTITUTION("Substitution", "🔄", EventSeverity.NORMAL),
    INJURY("Injury", "🏥", EventSeverity.NORMAL),
    OFFSIDE("Offside", "🚩", EventSeverity.MINOR),
    FOUL("Foul", "⚠️", EventSeverity.MINOR),
    CORNER("Corner", "📐", EventSeverity.MINOR),
    FREE_KICK("Free Kick", "🦶", EventSeverity.MINOR),
    PENALTY("Penalty", "⚽", EventSeverity.MAJOR),
    SAVE("Save", "🥅", EventSeverity.NORMAL),
    SHOT_ON_TARGET("Shot on Target", "🎯", EventSeverity.MINOR),
    SHOT_OFF_TARGET("Shot off Target", "❌", EventSeverity.MINOR),
    POSSESSION_CHANGE("Possession Change", "🔄", EventSeverity.MINOR),
    TACTICAL_CHANGE("Tactical Change", "📋", EventSeverity.NORMAL),
    HALF_TIME("Half Time", "⏸️", EventSeverity.NORMAL),
    FULL_TIME("Full Time", "⏹️", EventSeverity.MAJOR),
    KICK_OFF("Kick Off", "⚽", EventSeverity.NORMAL);
    
    private final String displayName;
    private final String icon;
    private final EventSeverity defaultSeverity;
}

public enum MatchPhase {
    PRE_MATCH("Pre-Match", 0),
    FIRST_HALF("First Half", 45),
    HALF_TIME("Half Time", 0),
    SECOND_HALF("Second Half", 45),
    EXTRA_TIME_FIRST("Extra Time 1st", 15),
    EXTRA_TIME_SECOND("Extra Time 2nd", 15),
    PENALTIES("Penalties", 0),
    FINISHED("Finished", 0);
    
    private final String displayName;
    private final int duration;
}

public enum EventSeverity {
    MINOR, NORMAL, MAJOR, CRITICAL
}

public enum CardType {
    YELLOW, RED, SECOND_YELLOW
}

public enum MatchIntensity {
    LOW, MODERATE, HIGH, EXTREME
}
```

### Service Layer Implementation

#### LiveMatchService
```java
@Service
public class LiveMatchService {
    
    @Autowired
    private LiveMatchSessionRepository liveMatchSessionRepository;
    
    @Autowired
    private MatchEventRepository matchEventRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private MatchService matchService;
    
    /**
     * Start live match session
     */
    public LiveMatchSession startLiveMatch(Long matchId) {
        Match match = matchService.findById(matchId);
        
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Match is not scheduled for live viewing");
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
        matchService.save(match);
        
        // Create kick-off event
        createMatchEvent(session, null, null, EventType.KICK_OFF, 0, 
                        "Match kicks off!", "The referee blows the whistle to start the match.");
        
        // Start match simulation in background
        startMatchSimulation(session);
        
        return session;
    }
    
    /**
     * Simulate match events in real-time
     */
    @Async
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
            Thread.sleep(30000); // 30 seconds = 15 minutes half time
            
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
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();
        
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
        String description = generateEventDescription(eventType, player, team);
        String detailedDescription = generateDetailedEventDescription(eventType, player, team, session);
        
        MatchEvent event = createMatchEvent(session, team, player, eventType, minute, 
                                          description, detailedDescription);
        
        // Process event effects
        switch (eventType) {
            case GOAL -> processGoalEvent(session, event, team);
            case YELLOW_CARD -> processCardEvent(session, event, CardType.YELLOW);
            case RED_CARD -> processCardEvent(session, event, CardType.RED);
            case SUBSTITUTION -> processSubstitutionEvent(session, event, team);
            case INJURY -> processInjuryEvent(session, event, player);
            case PENALTY -> processPenaltyEvent(session, event, team);
        }
        
        // Broadcast event to live viewers
        broadcastMatchEvent(event);
        
        // Update match statistics
        updateMatchStatistics(session, event);
    }
    
    /**
     * Process goal event
     */
    private void processGoalEvent(LiveMatchSession session, MatchEvent event, Team scoringTeam) {
        if (scoringTeam.equals(session.getMatch().getHomeTeam())) {
            session.setHomeScore(session.getHomeScore() + 1);
        } else {
            session.setAwayScore(session.getAwayScore() + 1);
        }
        
        event.setHomeScore(session.getHomeScore());
        event.setAwayScore(session.getAwayScore());
        
        // Check for assist
        if (RandomUtils.randomValue(0.0, 1.0) < 0.6) { // 60% chance of assist
            Player assistPlayer = selectAssistPlayer(scoringTeam, event.getPlayer());
            if (assistPlayer != null) {
                event.setAssistPlayer(assistPlayer);
                
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
    
    /**
     * Create match event
     */
    private MatchEvent createMatchEvent(LiveMatchSession session, Team team, Player player, 
                                      EventType eventType, int minute, String description, 
                                      String detailedDescription) {
        MatchEvent event = MatchEvent.builder()
            .match(session.getMatch())
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
        session.getEvents().add(event);
        
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
        MatchEventDTO eventDTO = MatchEventDTO.builder()
            .id(event.getId())
            .matchId(event.getMatch().getId())
            .eventType(event.getEventType())
            .minute(event.getMinute())
            .additionalTime(event.getAdditionalTime())
            .description(event.getDescription())
            .detailedDescription(event.getDetailedDescription())
            .playerName(event.getPlayer() != null ? 
                       event.getPlayer().getName() + " " + event.getPlayer().getSurname() : null)
            .teamName(event.getTeam() != null ? event.getTeam().getClub().getName() : null)
            .homeScore(event.getHomeScore())
            .awayScore(event.getAwayScore())
            .severity(event.getSeverity())
            .isKeyEvent(event.getIsKeyEvent())
            .build();
            
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
        session.setSpectatorCount(session.getSpectatorCount() + 1);
        liveMatchSessionRepository.save(session);
        
        // Send current match state to new viewer
        broadcastMatchUpdate(session);
        
        // Send recent events
        List<MatchEvent> recentEvents = session.getEvents().stream()
            .filter(event -> event.getEventTime().isAfter(LocalDateTime.now().minusMinutes(5)))
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
        session.setSpectatorCount(Math.max(0, session.getSpectatorCount() - 1));
        liveMatchSessionRepository.save(session);
    }
    
    private double calculateEventProbability(LiveMatchSession session, int minute) {
        double baseProbability = 0.15; // 15% chance per minute
        
        // Increase probability in final minutes
        if (minute > 80) baseProbability *= 1.5;
        if (minute > 85) baseProbability *= 1.3;
        
        // Adjust based on match intensity
        switch (session.getIntensity()) {
            case LOW -> baseProbability *= 0.7;
            case HIGH -> baseProbability *= 1.3;
            case EXTREME -> baseProbability *= 1.6;
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
            EventType.SAVE, 0.03,
            EventType.RED_CARD, 0.01
        );
        
        return RandomUtils.weightedRandomSelection(eventWeights);
    }
    
    private String generateEventDescription(EventType eventType, Player player, Team team) {
        String playerName = player != null ? player.getName() + " " + player.getSurname() : "";
        String teamName = team != null ? team.getClub().getName() : "";
        
        return switch (eventType) {
            case GOAL -> playerName + " scores for " + teamName + "!";
            case YELLOW_CARD -> playerName + " receives a yellow card";
            case RED_CARD -> playerName + " is sent off!";
            case SUBSTITUTION -> "Substitution for " + teamName;
            case SHOT_ON_TARGET -> playerName + " shoots on target";
            case SHOT_OFF_TARGET -> playerName + " shoots wide";
            case SAVE -> "Great save by the goalkeeper!";
            case CORNER -> "Corner kick for " + teamName;
            case FREE_KICK -> "Free kick awarded to " + teamName;
            case FOUL -> "Foul by " + playerName;
            case OFFSIDE -> playerName + " caught offside";
            default -> eventType.getDisplayName();
        };
    }
}
```

### WebSocket Configuration

#### WebSocketConfig
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/live-match")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

### API Endpoints

#### LiveMatchController
```java
@RestController
@RequestMapping("/api/live-match")
public class LiveMatchController {
    
    @Autowired
    private LiveMatchService liveMatchService;
    
    @PostMapping("/{matchId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveMatchSessionDTO> startLiveMatch(@PathVariable Long matchId) {
        LiveMatchSession session = liveMatchService.startLiveMatch(matchId);
        return ResponseEntity.ok(convertToDTO(session));
    }
    
    @GetMapping("/{matchId}")
    public ResponseEntity<LiveMatchSessionDTO> getLiveMatch(@PathVariable Long matchId) {
        LiveMatchSession session = liveMatchService.getLiveMatchSession(matchId);
        return ResponseEntity.ok(convertToDTO(session));
    }
    
    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEventDTO>> getMatchEvents(
            @PathVariable Long matchId,
            @RequestParam(required = false) Integer fromMinute) {
        List<MatchEvent> events = liveMatchService.getMatchEvents(matchId, fromMinute);
        return ResponseEntity.ok(events.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/{matchId}/join")
    public ResponseEntity<Void> joinLiveMatch(
            @PathVariable Long matchId,
            Authentication authentication) {
        String userId = authentication.getName();
        liveMatchService.joinLiveMatch(matchId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{matchId}/leave")
    public ResponseEntity<Void> leaveLiveMatch(@PathVariable Long matchId) {
        liveMatchService.leaveLiveMatch(matchId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<LiveMatchSessionDTO>> getActiveLiveMatches() {
        List<LiveMatchSession> sessions = liveMatchService.getActiveLiveMatches();
        return ResponseEntity.ok(sessions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### LiveMatchViewer Component (fm-web)
```jsx
import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getLiveMatch, joinLiveMatch, leaveLiveMatch } from '../services/api';

const LiveMatchViewer = ({ matchId }) => {
    const [matchSession, setMatchSession] = useState(null);
    const [events, setEvents] = useState([]);
    const [isConnected, setIsConnected] = useState(false);
    const [loading, setLoading] = useState(true);
    const stompClient = useRef(null);
    const eventsEndRef = useRef(null);

    useEffect(() => {
        loadMatchData();
        connectWebSocket();
        
        return () => {
            disconnectWebSocket();
        };
    }, [matchId]);

    useEffect(() => {
        scrollToBottom();
    }, [events]);

    const loadMatchData = async () => {
        try {
            const response = await getLiveMatch(matchId);
            setMatchSession(response.data);
            setEvents(response.data.events || []);
            
            // Join as spectator
            await joinLiveMatch(matchId);
        } catch (error) {
            console.error('Error loading match data:', error);
        } finally {
            setLoading(false);
        }
    };

    const connectWebSocket = () => {
        const socket = new SockJS('/ws/live-match');
        stompClient.current = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setIsConnected(true);
                
                // Subscribe to match updates
                stompClient.current.subscribe(`/topic/match/${matchId}`, (message) => {
                    const update = JSON.parse(message.body);
                    setMatchSession(prev => ({
                        ...prev,
                        ...update
                    }));
                });
                
                // Subscribe to match events
                stompClient.current.subscribe(`/topic/match/${matchId}/events`, (message) => {
                    const event = JSON.parse(message.body);
                    setEvents(prev => [...prev, event]);
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
            }
        });
        
        stompClient.current.activate();
    };

    const disconnectWebSocket = async () => {
        if (stompClient.current) {
            stompClient.current.deactivate();
        }
        
        try {
            await leaveLiveMatch(matchId);
        } catch (error) {
            console.error('Error leaving match:', error);
        }
    };

    const scrollToBottom = () => {
        eventsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const getEventIcon = (eventType) => {
        const icons = {
            GOAL: '⚽',
            YELLOW_CARD: '🟨',
            RED_CARD: '🟥',
            SUBSTITUTION: '🔄',
            CORNER: '📐',
            FREE_KICK: '🦶',
            SAVE: '🥅',
            SHOT_ON_TARGET: '🎯',
            SHOT_OFF_TARGET: '❌',
            FOUL: '⚠️',
            OFFSIDE: '🚩',
            INJURY: '🏥',
            HALF_TIME: '⏸️',
            FULL_TIME: '⏹️'
        };
        return icons[eventType] || '⚪';
    };

    const getEventSeverityClass = (severity) => {
        return `event-${severity.toLowerCase()}`;
    };

    const formatMatchTime = (minute, additionalTime) => {
        if (additionalTime > 0) {
            return `${minute}+${additionalTime}'`;
        }
        return `${minute}'`;
    };

    const getPhaseDisplay = (phase) => {
        const phases = {
            PRE_MATCH: 'Pre-Match',
            FIRST_HALF: '1st Half',
            HALF_TIME: 'Half Time',
            SECOND_HALF: '2nd Half',
            EXTRA_TIME_FIRST: 'Extra Time 1st',
            EXTRA_TIME_SECOND: 'Extra Time 2nd',
            PENALTIES: 'Penalties',
            FINISHED: 'Full Time'
        };
        return phases[phase] || phase;
    };

    const getIntensityColor = (intensity) => {
        const colors = {
            LOW: '#4caf50',
            MODERATE: '#ff9800',
            HIGH: '#f44336',
            EXTREME: '#9c27b0'
        };
        return colors[intensity] || '#666';
    };

    if (loading) return <div className="live-match-loading">Loading live match...</div>;
    if (!matchSession) return <div className="live-match-error">Match not found</div>;

    return (
        <div className="live-match-viewer">
            <div className="match-header">
                <div className="connection-status">
                    <span className={`status-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
                        {isConnected ? '🟢 LIVE' : '🔴 DISCONNECTED'}
                    </span>
                    <span className="spectator-count">👥 {matchSession.spectatorCount} watching</span>
                </div>
                
                <div className="match-info">
                    <div className="teams">
                        <div className="home-team">
                            <span className="team-name">{matchSession.match.homeTeam.club.name}</span>
                            <span className="team-score">{matchSession.homeScore}</span>
                        </div>
                        <div className="match-time">
                            <div className="time">
                                {formatMatchTime(matchSession.currentMinute, matchSession.additionalTime)}
                            </div>
                            <div className="phase">{getPhaseDisplay(matchSession.currentPhase)}</div>
                        </div>
                        <div className="away-team">
                            <span className="team-score">{matchSession.awayScore}</span>
                            <span className="team-name">{matchSession.match.awayTeam.club.name}</span>
                        </div>
                    </div>
                </div>
                
                <div className="match-conditions">
                    <div className="weather">
                        🌤️ {matchSession.weatherConditions} {matchSession.temperature}°C
                    </div>
                    <div 
                        className="intensity"
                        style={{ color: getIntensityColor(matchSession.intensity) }}
                    >
                        Intensity: {matchSession.intensity}
                    </div>
                </div>
            </div>

            <div className="match-events">
                <div className="events-header">
                    <h3>Match Events</h3>
                    <button onClick={scrollToBottom} className="scroll-to-bottom">
                        ⬇️ Latest
                    </button>
                </div>
                
                <div className="events-timeline">
                    {events.map(event => (
                        <div 
                            key={event.id} 
                            className={`event-item ${getEventSeverityClass(event.severity)} ${event.isKeyEvent ? 'key-event' : ''}`}
                        >
                            <div className="event-time">
                                {formatMatchTime(event.minute, event.additionalTime)}
                            </div>
                            <div className="event-icon">
                                {getEventIcon(event.eventType)}
                            </div>
                            <div className="event-content">
                                <div className="event-description">
                                    {event.description}
                                </div>
                                {event.detailedDescription && (
                                    <div className="event-details">
                                        {event.detailedDescription}
                                    </div>
                                )}
                                {event.playerName && (
                                    <div className="event-player">
                                        {event.playerName} ({event.teamName})
                                    </div>
                                )}
                            </div>
                            {(event.eventType === 'GOAL' || event.homeScore !== null) && (
                                <div className="event-score">
                                    {event.homeScore} - {event.awayScore}
                                </div>
                            )}
                        </div>
                    ))}
                    <div ref={eventsEndRef} />
                </div>
            </div>

            <div className="match-stats-summary">
                <div className="stat-item">
                    <span>Goals:</span>
                    <span>{events.filter(e => e.eventType === 'GOAL').length}</span>
                </div>
                <div className="stat-item">
                    <span>Cards:</span>
                    <span>{events.filter(e => e.eventType === 'YELLOW_CARD' || e.eventType === 'RED_CARD').length}</span>
                </div>
                <div className="stat-item">
                    <span>Substitutions:</span>
                    <span>{events.filter(e => e.eventType === 'SUBSTITUTION').length}</span>
                </div>
            </div>
        </div>
    );
};

export default LiveMatchViewer;
```

### CSS Styles

#### LiveMatchViewer.css
```css
.live-match-viewer {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    font-family: 'Arial', sans-serif;
}

.match-header {
    background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
    color: white;
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 20px;
}

.connection-status {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    font-size: 14px;
}

.status-indicator.connected {
    color: #4caf50;
    font-weight: bold;
}

.status-indicator.disconnected {
    color: #f44336;
    font-weight: bold;
}

.teams {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 20px 0;
}

.home-team, .away-team {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
}

.team-name {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
}

.team-score {
    font-size: 36px;
    font-weight: bold;
    color: #ffd700;
}

.match-time {
    text-align: center;
    flex: 1;
}

.time {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 5px;
}

.phase {
    font-size: 14px;
    opacity: 0.8;
}

.match-conditions {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    opacity: 0.9;
}

.match-events {
    background: white;
    border-radius: 10px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    overflow: hidden;
}

.events-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: #f5f5f5;
    border-bottom: 1px solid #ddd;
}

.events-timeline {
    max-height: 600px;
    overflow-y: auto;
    padding: 10px;
}

.event-item {
    display: flex;
    align-items: flex-start;
    padding: 10px;
    margin-bottom: 10px;
    border-radius: 8px;
    border-left: 4px solid #ddd;
    background: #fafafa;
    transition: all 0.3s ease;
}

.event-item.key-event {
    background: #fff3cd;
    border-left-color: #ffc107;
    box-shadow: 0 2px 5px rgba(255,193,7,0.2);
}

.event-item.event-major {
    background: #f8d7da;
    border-left-color: #dc3545;
}

.event-item.event-critical {
    background: #d1ecf1;
    border-left-color: #17a2b8;
}

.event-time {
    min-width: 50px;
    font-weight: bold;
    color: #666;
    font-size: 14px;
}

.event-icon {
    font-size: 20px;
    margin: 0 10px;
}

.event-content {
    flex: 1;
}

.event-description {
    font-weight: bold;
    margin-bottom: 5px;
}

.event-details {
    font-size: 14px;
    color: #666;
    margin-bottom: 5px;
}

.event-player {
    font-size: 12px;
    color: #888;
}

.event-score {
    font-weight: bold;
    font-size: 18px;
    color: #2a5298;
    min-width: 60px;
    text-align: right;
}

.match-stats-summary {
    display: flex;
    justify-content: space-around;
    background: white;
    padding: 15px;
    border-radius: 10px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    margin-top: 20px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
}

.stat-item span:first-child {
    font-size: 14px;
    color: #666;
}

.stat-item span:last-child {
    font-size: 20px;
    font-weight: bold;
    color: #2a5298;
}

.scroll-to-bottom {
    background: #2a5298;
    color: white;
    border: none;
    padding: 5px 10px;
    border-radius: 5px;
    cursor: pointer;
    font-size: 12px;
}

.scroll-to-bottom:hover {
    background: #1e3c72;
}

@keyframes newEvent {
    0% {
        transform: translateX(-100%);
        opacity: 0;
    }
    100% {
        transform: translateX(0);
        opacity: 1;
    }
}

.event-item:last-child {
    animation: newEvent 0.5s ease-out;
}
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class LiveMatchServiceTest {
    
    @Mock
    private LiveMatchSessionRepository liveMatchSessionRepository;
    
    @Mock
    private MatchEventRepository matchEventRepository;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @InjectMocks
    private LiveMatchService liveMatchService;
    
    @Test
    void testStartLiveMatch() {
        Match match = createTestMatch();
        when(matchService.findById(1L)).thenReturn(match);
        
        LiveMatchSession session = liveMatchService.startLiveMatch(1L);
        
        assertThat(session.getMatch()).isEqualTo(match);
        assertThat(session.getCurrentPhase()).isEqualTo(MatchPhase.PRE_MATCH);
        assertThat(session.getHomeScore()).isEqualTo(0);
        assertThat(session.getAwayScore()).isEqualTo(0);
    }
    
    @Test
    void testEventCreation() {
        LiveMatchSession session = createTestSession();
        Team team = createTestTeam();
        Player player = createTestPlayer();
        
        MatchEvent event = liveMatchService.createMatchEvent(
            session, team, player, EventType.GOAL, 25, "Goal!", "Great goal!");
        
        assertThat(event.getEventType()).isEqualTo(EventType.GOAL);
        assertThat(event.getMinute()).isEqualTo(25);
        assertThat(event.getPlayer()).isEqualTo(player);
    }
    
    @Test
    void testGoalProcessing() {
        LiveMatchSession session = createTestSession();
        Team homeTeam = session.getMatch().getHomeTeam();
        MatchEvent goalEvent = createTestGoalEvent();
        
        liveMatchService.processGoalEvent(session, goalEvent, homeTeam);
        
        assertThat(session.getHomeScore()).isEqualTo(1);
        verify(messagingTemplate).convertAndSend(anyString(), any());
    }
}
```

### Configuration

#### Application Properties
```properties
# Live match configuration
fm.live-match.simulation.speed=1000
fm.live-match.event.probability.base=0.15
fm.live-match.additional-time.max=5
fm.live-match.websocket.heartbeat=30000
fm.live-match.spectator.timeout=300000
```

## Implementation Notes

1. **Performance**: WebSocket connections should be managed efficiently to handle multiple concurrent viewers
2. **Scalability**: Consider using Redis for WebSocket session management in production
3. **Real-time Accuracy**: Match simulation timing should be configurable for different viewing experiences
4. **Event Quality**: Event descriptions should be varied and contextual to maintain engagement
5. **Mobile Support**: Ensure WebSocket connections work reliably on mobile devices
6. **Offline Handling**: Gracefully handle connection drops and reconnection
7. **Data Persistence**: All events should be persisted for post-match analysis

## Dependencies

- WebSocket support (Spring WebSocket + STOMP)
- Match simulation system
- Player and team data
- Real-time messaging infrastructure
- Frontend WebSocket client libraries
- Match statistics system