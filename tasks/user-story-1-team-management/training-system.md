# Training System Implementation

## Overview
Implement a comprehensive training system allowing managers to set weekly training focus to improve specific player abilities.

## Technical Requirements

### Database Schema Changes

#### New Entity: TrainingSession
```java
@Entity
@Table(name = "training_session")
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    
    @Enumerated(EnumType.STRING)
    private TrainingFocus focus;
    
    @Enumerated(EnumType.STRING)
    private TrainingIntensity intensity;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private TrainingStatus status; // PLANNED, ACTIVE, COMPLETED
    
    private Double effectivenessMultiplier; // Based on facilities, staff, etc.
    
    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL)
    private List<PlayerTrainingResult> playerResults = new ArrayList<>();
}
```

#### New Entity: PlayerTrainingResult
```java
@Entity
@Table(name = "player_training_result")
public class PlayerTrainingResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_session_id")
    private TrainingSession trainingSession;
    
    private Double attendanceRate; // 0.0 to 1.0
    private Double improvementGained; // Skill points gained
    private Double fatigueGained; // Condition lost
    
    @Enumerated(EnumType.STRING)
    private TrainingPerformance performance; // POOR, AVERAGE, GOOD, EXCELLENT
}
```

#### Enums to Create
```java
public enum TrainingFocus {
    ATTACKING("Attacking", List.of("scoring", "winger", "passing")),
    DEFENDING("Defending", List.of("defending", "playmaking")),
    PHYSICAL("Physical", List.of("stamina")),
    TECHNICAL("Technical", List.of("passing", "setPieces")),
    GOALKEEPING("Goalkeeping", List.of("goalkeeping", "setPieces")),
    BALANCED("Balanced", List.of("playmaking", "passing", "defending"));
    
    private final String displayName;
    private final List<String> affectedSkills;
}

public enum TrainingIntensity {
    LIGHT(0.5, 0.1),     // Low improvement, low fatigue
    MODERATE(1.0, 0.3),  // Normal improvement, normal fatigue
    INTENSIVE(1.5, 0.6), // High improvement, high fatigue
    RECOVERY(0.2, -0.5); // Minimal improvement, condition recovery
    
    private final double improvementMultiplier;
    private final double fatigueMultiplier;
}

public enum TrainingPerformance {
    POOR(0.5), AVERAGE(1.0), GOOD(1.3), EXCELLENT(1.6);
    
    private final double multiplier;
}
```

#### Team Entity Updates
```java
// Add to Team.java
@OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
private TrainingPlan currentTrainingPlan;

@OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
private List<TrainingSession> trainingHistory = new ArrayList<>();
```

#### New Entity: TrainingPlan
```java
@Entity
@Table(name = "training_plan")
public class TrainingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    
    @Enumerated(EnumType.STRING)
    private TrainingFocus mondayFocus;
    
    @Enumerated(EnumType.STRING)
    private TrainingFocus tuesdayFocus;
    
    @Enumerated(EnumType.STRING)
    private TrainingFocus wednesdayFocus;
    
    @Enumerated(EnumType.STRING)
    private TrainingFocus thursdayFocus;
    
    @Enumerated(EnumType.STRING)
    private TrainingFocus fridayFocus;
    
    @Enumerated(EnumType.STRING)
    private TrainingIntensity intensity;
    
    private Boolean restOnWeekends;
    
    private LocalDateTime lastUpdated;
}
```

### Service Layer Implementation

#### TrainingService
```java
@Service
public class TrainingService {
    
    @Autowired
    private TrainingSessionRepository trainingSessionRepository;
    
    @Autowired
    private PlayerTrainingResultRepository playerTrainingResultRepository;
    
    @Autowired
    private TrainingPlanRepository trainingPlanRepository;
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * Process daily training for all teams
     */
    @Scheduled(cron = "0 0 10 * * MON-FRI") // Weekdays at 10 AM
    public void processDailyTraining() {
        List<TrainingPlan> activePlans = trainingPlanRepository.findAll();
        
        for (TrainingPlan plan : activePlans) {
            if (shouldTrainToday(plan)) {
                TrainingFocus todaysFocus = getTodaysFocus(plan);
                processTeamTraining(plan.getTeam(), todaysFocus, plan.getIntensity());
            }
        }
    }
    
    /**
     * Process training session for a team
     */
    public TrainingSession processTeamTraining(Team team, TrainingFocus focus, 
                                             TrainingIntensity intensity) {
        TrainingSession session = TrainingSession.builder()
            .team(team)
            .focus(focus)
            .intensity(intensity)
            .startDate(LocalDate.now())
            .status(TrainingStatus.ACTIVE)
            .effectivenessMultiplier(calculateEffectiveness(team))
            .build();
        
        session = trainingSessionRepository.save(session);
        
        // Process each player's training
        List<Player> players = team.getPlayers();
        for (Player player : players) {
            if (!player.isInjured() && player.getCondition() > 20) {
                PlayerTrainingResult result = processPlayerTraining(player, session);
                session.getPlayerResults().add(result);
            }
        }
        
        session.setStatus(TrainingStatus.COMPLETED);
        session.setEndDate(LocalDate.now());
        
        return trainingSessionRepository.save(session);
    }
    
    /**
     * Process individual player training
     */
    private PlayerTrainingResult processPlayerTraining(Player player, 
                                                     TrainingSession session) {
        // Calculate attendance based on player condition and morale
        double attendanceRate = calculateAttendanceRate(player);
        
        // Calculate performance based on player attributes and randomness
        TrainingPerformance performance = calculateTrainingPerformance(player, session);
        
        // Calculate skill improvement
        double baseImprovement = 0.1; // Base skill points per session
        double improvement = baseImprovement * 
                           session.getIntensity().getImprovementMultiplier() *
                           performance.getMultiplier() *
                           session.getEffectivenessMultiplier() *
                           attendanceRate;
        
        // Apply improvement to relevant skills
        applySkillImprovement(player, session.getFocus(), improvement);
        
        // Calculate and apply fatigue
        double fatigue = session.getIntensity().getFatigueMultiplier() * 
                        attendanceRate * 10; // 10 condition points base
        player.decrementCondition(fatigue);
        
        // Save player changes
        playerService.save(player);
        
        // Create training result record
        PlayerTrainingResult result = PlayerTrainingResult.builder()
            .player(player)
            .trainingSession(session)
            .attendanceRate(attendanceRate)
            .improvementGained(improvement)
            .fatigueGained(fatigue)
            .performance(performance)
            .build();
            
        return playerTrainingResultRepository.save(result);
    }
    
    /**
     * Apply skill improvements based on training focus
     */
    private void applySkillImprovement(Player player, TrainingFocus focus, 
                                     double improvement) {
        List<String> affectedSkills = focus.getAffectedSkills();
        double improvementPerSkill = improvement / affectedSkills.size();
        
        for (String skill : affectedSkills) {
            switch (skill) {
                case "scoring" -> player.setScoring(
                    Math.min(99.0, player.getScoring() + improvementPerSkill));
                case "winger" -> player.setWinger(
                    Math.min(99.0, player.getWinger() + improvementPerSkill));
                case "passing" -> player.setPassing(
                    Math.min(99.0, player.getPassing() + improvementPerSkill));
                case "defending" -> player.setDefending(
                    Math.min(99.0, player.getDefending() + improvementPerSkill));
                case "playmaking" -> player.setPlaymaking(
                    Math.min(99.0, player.getPlaymaking() + improvementPerSkill));
                case "stamina" -> player.setStamina(
                    Math.min(99.0, player.getStamina() + improvementPerSkill));
                case "goalkeeping" -> player.setGoalkeeping(
                    Math.min(99.0, player.getGoalkeeping() + improvementPerSkill));
                case "setPieces" -> player.setSetPieces(
                    Math.min(99.0, player.getSetPieces() + improvementPerSkill));
            }
        }
    }
    
    /**
     * Calculate training effectiveness based on facilities and staff
     */
    private Double calculateEffectiveness(Team team) {
        double baseEffectiveness = 1.0;
        
        // TODO: Add facility bonuses when infrastructure system is implemented
        // Club facilities = team.getClub().getFacilities();
        // if (facilities.getTrainingCenter() != null) {
        //     baseEffectiveness += facilities.getTrainingCenter().getBonus();
        // }
        
        // TODO: Add staff bonuses when staff system is implemented
        // List<Staff> coaches = team.getClub().getStaff().stream()
        //     .filter(s -> s.getRole() == StaffRole.COACH)
        //     .collect(Collectors.toList());
        // baseEffectiveness += coaches.size() * 0.1;
        
        return Math.min(2.0, baseEffectiveness); // Cap at 2x effectiveness
    }
    
    /**
     * Update team's training plan
     */
    public TrainingPlan updateTrainingPlan(Long teamId, TrainingPlanRequest request) {
        Team team = teamService.findById(teamId);
        
        TrainingPlan plan = team.getCurrentTrainingPlan();
        if (plan == null) {
            plan = new TrainingPlan();
            plan.setTeam(team);
        }
        
        plan.setMondayFocus(request.getMondayFocus());
        plan.setTuesdayFocus(request.getTuesdayFocus());
        plan.setWednesdayFocus(request.getWednesdayFocus());
        plan.setThursdayFocus(request.getThursdayFocus());
        plan.setFridayFocus(request.getFridayFocus());
        plan.setIntensity(request.getIntensity());
        plan.setRestOnWeekends(request.getRestOnWeekends());
        plan.setLastUpdated(LocalDateTime.now());
        
        return trainingPlanRepository.save(plan);
    }
    
    private TrainingFocus getTodaysFocus(TrainingPlan plan) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return switch (today) {
            case MONDAY -> plan.getMondayFocus();
            case TUESDAY -> plan.getTuesdayFocus();
            case WEDNESDAY -> plan.getWednesdayFocus();
            case THURSDAY -> plan.getThursdayFocus();
            case FRIDAY -> plan.getFridayFocus();
            default -> null; // Weekend or rest day
        };
    }
    
    private boolean shouldTrainToday(TrainingPlan plan) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        
        if (plan.getRestOnWeekends() && 
            (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY)) {
            return false;
        }
        
        return getTodaysFocus(plan) != null;
    }
}
```

### API Endpoints

#### TrainingController
```java
@RestController
@RequestMapping("/api/training")
public class TrainingController {
    
    @Autowired
    private TrainingService trainingService;
    
    @GetMapping("/plan/{teamId}")
    public ResponseEntity<TrainingPlanDTO> getTrainingPlan(
            @PathVariable Long teamId) {
        TrainingPlan plan = trainingService.getTrainingPlan(teamId);
        return ResponseEntity.ok(convertToDTO(plan));
    }
    
    @PutMapping("/plan/{teamId}")
    public ResponseEntity<TrainingPlanDTO> updateTrainingPlan(
            @PathVariable Long teamId,
            @RequestBody TrainingPlanRequest request) {
        TrainingPlan plan = trainingService.updateTrainingPlan(teamId, request);
        return ResponseEntity.ok(convertToDTO(plan));
    }
    
    @GetMapping("/history/{teamId}")
    public ResponseEntity<Page<TrainingSessionDTO>> getTrainingHistory(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TrainingSession> sessions = trainingService
            .getTrainingHistory(teamId, PageRequest.of(page, size));
        return ResponseEntity.ok(sessions.map(this::convertToDTO));
    }
    
    @GetMapping("/session/{sessionId}/results")
    public ResponseEntity<List<PlayerTrainingResultDTO>> getSessionResults(
            @PathVariable Long sessionId) {
        List<PlayerTrainingResult> results = trainingService
            .getSessionResults(sessionId);
        return ResponseEntity.ok(results.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/session/manual/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingSessionDTO> createManualSession(
            @PathVariable Long teamId,
            @RequestBody ManualTrainingRequest request) {
        TrainingSession session = trainingService
            .createManualTrainingSession(teamId, request);
        return ResponseEntity.ok(convertToDTO(session));
    }
}
```

### Frontend Implementation

#### TrainingPlan Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getTrainingPlan, updateTrainingPlan } from '../services/api';

const TrainingPlan = ({ teamId }) => {
    const [plan, setPlan] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const trainingFocusOptions = [
        { value: 'ATTACKING', label: 'Attacking', description: 'Improves scoring, winger, passing' },
        { value: 'DEFENDING', label: 'Defending', description: 'Improves defending, playmaking' },
        { value: 'PHYSICAL', label: 'Physical', description: 'Improves stamina' },
        { value: 'TECHNICAL', label: 'Technical', description: 'Improves passing, set pieces' },
        { value: 'GOALKEEPING', label: 'Goalkeeping', description: 'Improves goalkeeping, set pieces' },
        { value: 'BALANCED', label: 'Balanced', description: 'Improves all skills moderately' }
    ];

    const intensityOptions = [
        { value: 'LIGHT', label: 'Light', description: 'Low improvement, low fatigue' },
        { value: 'MODERATE', label: 'Moderate', description: 'Normal improvement, normal fatigue' },
        { value: 'INTENSIVE', label: 'Intensive', description: 'High improvement, high fatigue' },
        { value: 'RECOVERY', label: 'Recovery', description: 'Minimal improvement, restores condition' }
    ];

    useEffect(() => {
        loadTrainingPlan();
    }, [teamId]);

    const loadTrainingPlan = async () => {
        try {
            const response = await getTrainingPlan(teamId);
            setPlan(response.data);
        } catch (error) {
            console.error('Error loading training plan:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            const response = await updateTrainingPlan(teamId, plan);
            setPlan(response.data);
            // Show success notification
        } catch (error) {
            console.error('Error saving training plan:', error);
            // Show error notification
        } finally {
            setSaving(false);
        }
    };

    const handleFocusChange = (day, focus) => {
        setPlan(prev => ({
            ...prev,
            [`${day}Focus`]: focus
        }));
    };

    if (loading) return <div>Loading training plan...</div>;

    return (
        <div className="training-plan">
            <h2>Training Plan</h2>
            
            <div className="training-schedule">
                <h3>Weekly Schedule</h3>
                {['monday', 'tuesday', 'wednesday', 'thursday', 'friday'].map(day => (
                    <div key={day} className="training-day">
                        <label className="day-label">
                            {day.charAt(0).toUpperCase() + day.slice(1)}
                        </label>
                        <select
                            value={plan[`${day}Focus`] || ''}
                            onChange={(e) => handleFocusChange(day, e.target.value)}
                            className="focus-select"
                        >
                            <option value="">Rest Day</option>
                            {trainingFocusOptions.map(option => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                        {plan[`${day}Focus`] && (
                            <span className="focus-description">
                                {trainingFocusOptions.find(o => o.value === plan[`${day}Focus`])?.description}
                            </span>
                        )}
                    </div>
                ))}
            </div>

            <div className="training-intensity">
                <h3>Training Intensity</h3>
                {intensityOptions.map(option => (
                    <label key={option.value} className="intensity-option">
                        <input
                            type="radio"
                            name="intensity"
                            value={option.value}
                            checked={plan.intensity === option.value}
                            onChange={(e) => setPlan(prev => ({
                                ...prev,
                                intensity: e.target.value
                            }))}
                        />
                        <span className="intensity-label">{option.label}</span>
                        <span className="intensity-description">{option.description}</span>
                    </label>
                ))}
            </div>

            <div className="weekend-settings">
                <label className="weekend-rest">
                    <input
                        type="checkbox"
                        checked={plan.restOnWeekends || false}
                        onChange={(e) => setPlan(prev => ({
                            ...prev,
                            restOnWeekends: e.target.checked
                        }))}
                    />
                    Rest on weekends
                </label>
            </div>

            <div className="training-actions">
                <button 
                    onClick={handleSave}
                    disabled={saving}
                    className="save-button"
                >
                    {saving ? 'Saving...' : 'Save Training Plan'}
                </button>
            </div>
        </div>
    );
};

export default TrainingPlan;
```

#### TrainingHistory Component
```jsx
import React, { useState, useEffect } from 'react';
import { getTrainingHistory, getSessionResults } from '../services/api';

const TrainingHistory = ({ teamId }) => {
    const [sessions, setSessions] = useState([]);
    const [selectedSession, setSelectedSession] = useState(null);
    const [sessionResults, setSessionResults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadTrainingHistory();
    }, [teamId]);

    const loadTrainingHistory = async () => {
        try {
            const response = await getTrainingHistory(teamId);
            setSessions(response.data.content);
        } catch (error) {
            console.error('Error loading training history:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadSessionResults = async (sessionId) => {
        try {
            const response = await getSessionResults(sessionId);
            setSessionResults(response.data);
            setSelectedSession(sessionId);
        } catch (error) {
            console.error('Error loading session results:', error);
        }
    };

    const getPerformanceColor = (performance) => {
        const colors = {
            POOR: '#ff4444',
            AVERAGE: '#ffaa00',
            GOOD: '#44aa44',
            EXCELLENT: '#0088ff'
        };
        return colors[performance] || '#666';
    };

    if (loading) return <div>Loading training history...</div>;

    return (
        <div className="training-history">
            <h2>Training History</h2>
            
            <div className="sessions-list">
                {sessions.map(session => (
                    <div 
                        key={session.id} 
                        className={`session-card ${selectedSession === session.id ? 'selected' : ''}`}
                        onClick={() => loadSessionResults(session.id)}
                    >
                        <div className="session-header">
                            <span className="session-date">
                                {new Date(session.startDate).toLocaleDateString()}
                            </span>
                            <span className="session-focus">{session.focus}</span>
                            <span className="session-intensity">{session.intensity}</span>
                        </div>
                        <div className="session-stats">
                            <span>Players: {session.playerResults.length}</span>
                            <span>Effectiveness: {Math.round(session.effectivenessMultiplier * 100)}%</span>
                        </div>
                    </div>
                ))}
            </div>

            {selectedSession && sessionResults.length > 0 && (
                <div className="session-results">
                    <h3>Session Results</h3>
                    <div className="results-table">
                        <div className="results-header">
                            <span>Player</span>
                            <span>Attendance</span>
                            <span>Performance</span>
                            <span>Improvement</span>
                            <span>Fatigue</span>
                        </div>
                        {sessionResults.map(result => (
                            <div key={result.id} className="result-row">
                                <span className="player-name">
                                    {result.player.name} {result.player.surname}
                                </span>
                                <span className="attendance">
                                    {Math.round(result.attendanceRate * 100)}%
                                </span>
                                <span 
                                    className="performance"
                                    style={{ color: getPerformanceColor(result.performance) }}
                                >
                                    {result.performance}
                                </span>
                                <span className="improvement">
                                    +{result.improvementGained.toFixed(2)}
                                </span>
                                <span className="fatigue">
                                    -{result.fatigueGained.toFixed(1)}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default TrainingHistory;
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    
    @Mock
    private TrainingSessionRepository trainingSessionRepository;
    
    @Mock
    private PlayerService playerService;
    
    @InjectMocks
    private TrainingService trainingService;
    
    @Test
    void testSkillImprovementApplication() {
        Player player = createTestPlayer();
        double initialScoring = player.getScoring();
        
        trainingService.applySkillImprovement(player, TrainingFocus.ATTACKING, 1.0);
        
        assertThat(player.getScoring()).isGreaterThan(initialScoring);
    }
    
    @Test
    void testTrainingEffectivenessCalculation() {
        Team team = createTestTeam();
        
        Double effectiveness = trainingService.calculateEffectiveness(team);
        
        assertThat(effectiveness).isBetween(0.5, 2.0);
    }
    
    @Test
    void testInjuredPlayersSkipTraining() {
        Player injuredPlayer = createInjuredPlayer();
        Team team = createTestTeam();
        team.getPlayers().add(injuredPlayer);
        
        TrainingSession session = trainingService.processTeamTraining(
            team, TrainingFocus.ATTACKING, TrainingIntensity.MODERATE);
        
        assertThat(session.getPlayerResults()).isEmpty();
    }
}
```

### Configuration

#### Application Properties
```properties
# Training system configuration
fm.training.base-improvement=0.1
fm.training.max-skill-value=99.0
fm.training.min-condition-to-train=20.0
fm.training.effectiveness-cap=2.0
fm.training.schedule.time=10:00
```

## Implementation Notes

1. **Skill Caps**: All skills are capped at 99.0 to maintain game balance
2. **Age Factor**: Younger players should improve faster (implement age-based multipliers)
3. **Position Relevance**: Players training in their natural position should see better results
4. **Facility Integration**: System is designed to integrate with future facility upgrades
5. **Staff Integration**: Prepared for coaching staff bonuses
6. **Injury Prevention**: Low-intensity training should reduce injury risk
7. **Morale Impact**: Good training results should boost player morale

## Dependencies

- Spring Boot Scheduler for daily training processing
- Player service for skill updates
- Team management system
- Future: Facility management system
- Future: Staff management system
- Notification system for training reports