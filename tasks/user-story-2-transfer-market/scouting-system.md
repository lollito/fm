# Scouting System Implementation

## Overview
Implement a comprehensive scouting system that requires clubs to "observe" players from other clubs to reveal their exact abilities and receive notifications about their status changes.

## Technical Requirements

### Database Schema Changes

#### New Entity: Scout
```java
@Entity
@Table(name = "scout")
public class Scout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String surname;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Country scoutingRegion;
    
    private Integer ability; // 1-20, affects scouting accuracy and speed
    private Integer reputation; // 1-20, affects access to information
    private BigDecimal monthlySalary;
    
    @Enumerated(EnumType.STRING)
    private ScoutSpecialization specialization;
    
    @Enumerated(EnumType.STRING)
    private ScoutStatus status; // ACTIVE, INJURED, SUSPENDED
    
    private LocalDate contractEnd;
    private Integer experience; // Years of scouting experience
    
    @OneToMany(mappedBy = "scout", cascade = CascadeType.ALL)
    private List<ScoutingAssignment> assignments = new ArrayList<>();
}
```

#### New Entity: ScoutingAssignment
```java
@Entity
@Table(name = "scouting_assignment")
public class ScoutingAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scout_id")
    private Scout scout;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_player_id")
    private Player targetPlayer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_club_id")
    private Club targetClub; // For general club scouting
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_region_id")
    private Country targetRegion; // For regional scouting
    
    @Enumerated(EnumType.STRING)
    private ScoutingType type; // PLAYER, CLUB, REGION, OPPOSITION
    
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status; // ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    
    private LocalDate assignedDate;
    private LocalDate completionDate;
    private LocalDate expectedCompletionDate;
    
    private Integer priority; // 1-5, affects resource allocation
    private String instructions; // Special instructions for scout
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<ScoutingReport> reports = new ArrayList<>();
}
```

#### New Entity: ScoutingReport
```java
@Entity
@Table(name = "scouting_report")
public class ScoutingReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private ScoutingAssignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scout_id")
    private Scout scout;
    
    private LocalDate reportDate;
    
    // Revealed player attributes (with accuracy based on scout ability)
    private Double revealedStamina;
    private Double revealedPlaymaking;
    private Double revealedScoring;
    private Double revealedWinger;
    private Double revealedGoalkeeping;
    private Double revealedPassing;
    private Double revealedDefending;
    private Double revealedSetPieces;
    
    // Scout's assessment
    private Integer overallRating; // 1-100
    private Integer potentialRating; // 1-100
    private Double accuracyLevel; // 0.0-1.0, how accurate the report is
    
    @Enumerated(EnumType.STRING)
    private RecommendationLevel recommendation; // AVOID, MONITOR, CONSIDER, RECOMMEND, PRIORITY
    
    private String strengths; // Text description of player strengths
    private String weaknesses; // Text description of player weaknesses
    private String personalityAssessment;
    private String injuryHistory;
    
    // Market information
    private BigDecimal estimatedValue;
    private BigDecimal estimatedWage;
    private Boolean isAvailableForTransfer;
    private LocalDate contractExpiry;
    
    private String additionalNotes;
    private Integer confidenceLevel; // 1-10, scout's confidence in the report
}
```

#### New Entity: PlayerScoutingStatus
```java
@Entity
@Table(name = "player_scouting_status")
public class PlayerScoutingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club scoutingClub;
    
    @Enumerated(EnumType.STRING)
    private ScoutingLevel scoutingLevel; // UNKNOWN, BASIC, DETAILED, COMPREHENSIVE
    
    private LocalDate lastScoutedDate;
    private LocalDate firstScoutedDate;
    
    private Integer timesScoutedThisSeason;
    private Double knowledgeAccuracy; // 0.0-1.0, how accurate the club's knowledge is
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<ScoutingReport> reports = new ArrayList<>();
    
    // Cached revealed attributes (best known values)
    private Double knownStamina;
    private Double knownPlaymaking;
    private Double knownScoring;
    private Double knownWinger;
    private Double knownGoalkeeping;
    private Double knownPassing;
    private Double knownDefending;
    private Double knownSetPieces;
}
```

#### Enums to Create
```java
public enum ScoutSpecialization {
    GOALKEEPERS("Goalkeepers"),
    DEFENDERS("Defenders"),
    MIDFIELDERS("Midfielders"),
    FORWARDS("Forwards"),
    YOUTH_PLAYERS("Youth Players"),
    OPPOSITION_ANALYSIS("Opposition Analysis"),
    GENERAL("General Scouting");
    
    private final String displayName;
}

public enum ScoutingType {
    PLAYER("Individual Player"),
    CLUB("Club Overview"),
    REGION("Regional Scouting"),
    OPPOSITION("Opposition Analysis");
    
    private final String displayName;
}

public enum AssignmentStatus {
    ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
}

public enum ScoutingLevel {
    UNKNOWN("Unknown", 0.0),
    BASIC("Basic Knowledge", 0.3),
    DETAILED("Detailed Knowledge", 0.7),
    COMPREHENSIVE("Comprehensive Knowledge", 1.0);
    
    private final String displayName;
    private final double accuracyMultiplier;
}

public enum RecommendationLevel {
    AVOID("Avoid", "#f44336"),
    MONITOR("Monitor", "#ff9800"),
    CONSIDER("Consider", "#2196f3"),
    RECOMMEND("Recommend", "#4caf50"),
    PRIORITY("Priority Target", "#9c27b0");
    
    private final String displayName;
    private final String color;
}

public enum ScoutStatus {
    ACTIVE, INJURED, SUSPENDED
}
```

### Service Layer Implementation

#### ScoutingService
```java
@Service
public class ScoutingService {
    
    @Autowired
    private ScoutRepository scoutRepository;
    
    @Autowired
    private ScoutingAssignmentRepository assignmentRepository;
    
    @Autowired
    private ScoutingReportRepository reportRepository;
    
    @Autowired
    private PlayerScoutingStatusRepository scoutingStatusRepository;
    
    /**
     * Assign scout to scout a specific player
     */
    public ScoutingAssignment assignPlayerScouting(Long scoutId, Long playerId, 
                                                  Integer priority, String instructions) {
        Scout scout = scoutRepository.findById(scoutId)
            .orElseThrow(() -> new EntityNotFoundException("Scout not found"));
        Player player = playerService.findById(playerId);
        
        // Check if scout is available
        if (!isScoutAvailable(scout)) {
            throw new IllegalStateException("Scout is not available for new assignments");
        }
        
        // Check if player is already being scouted by this club
        Optional<ScoutingAssignment> existingAssignment = assignmentRepository
            .findActiveAssignmentForPlayer(scout.getClub(), player);
            
        if (existingAssignment.isPresent()) {
            throw new IllegalStateException("Player is already being scouted");
        }
        
        // Calculate expected completion time based on scout ability and player difficulty
        int daysToComplete = calculateScoutingDuration(scout, player, ScoutingType.PLAYER);
        
        ScoutingAssignment assignment = ScoutingAssignment.builder()
            .scout(scout)
            .targetPlayer(player)
            .type(ScoutingType.PLAYER)
            .status(AssignmentStatus.ASSIGNED)
            .assignedDate(LocalDate.now())
            .expectedCompletionDate(LocalDate.now().plusDays(daysToComplete))
            .priority(priority)
            .instructions(instructions)
            .build();
            
        assignment = assignmentRepository.save(assignment);
        
        // Start scouting process
        startScoutingProcess(assignment);
        
        return assignment;
    }
    
    /**
     * Process daily scouting progress
     */
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    public void processDailyScoutingProgress() {
        List<ScoutingAssignment> activeAssignments = assignmentRepository
            .findByStatus(AssignmentStatus.IN_PROGRESS);
            
        for (ScoutingAssignment assignment : activeAssignments) {
            processScoutingProgress(assignment);
        }
    }
    
    /**
     * Process individual scouting assignment progress
     */
    private void processScoutingProgress(ScoutingAssignment assignment) {
        Scout scout = assignment.getScout();
        
        // Check if scout is available (not injured, etc.)
        if (scout.getStatus() != ScoutStatus.ACTIVE) {
            // Pause assignment
            return;
        }
        
        // Calculate daily progress based on scout ability and assignment difficulty
        double dailyProgress = calculateDailyProgress(assignment);
        
        // Check if assignment should be completed
        if (LocalDate.now().isAfter(assignment.getExpectedCompletionDate()) ||
            RandomUtils.randomValue(0.0, 1.0) < dailyProgress) {
            completeScoutingAssignment(assignment);
        }
    }
    
    /**
     * Complete scouting assignment and generate report
     */
    private void completeScoutingAssignment(ScoutingAssignment assignment) {
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletionDate(LocalDate.now());
        
        // Generate scouting report
        ScoutingReport report = generateScoutingReport(assignment);
        
        // Update player scouting status
        updatePlayerScoutingStatus(assignment.getTargetPlayer(), 
                                 assignment.getScout().getClub(), report);
        
        assignmentRepository.save(assignment);
        
        // Notify club about completed scouting
        createScoutingCompletionNotification(assignment, report);
    }
    
    /**
     * Generate scouting report with accuracy based on scout ability
     */
    private ScoutingReport generateScoutingReport(ScoutingAssignment assignment) {
        Scout scout = assignment.getScout();
        Player player = assignment.getTargetPlayer();
        
        // Calculate accuracy based on scout ability, experience, and specialization
        double baseAccuracy = scout.getAbility() / 20.0; // 0.05 to 1.0
        double experienceBonus = Math.min(0.2, scout.getExperience() / 100.0); // Up to 20% bonus
        double specializationBonus = isPlayerInScoutSpecialization(player, scout) ? 0.1 : 0.0;
        
        double totalAccuracy = Math.min(0.95, baseAccuracy + experienceBonus + specializationBonus);
        
        ScoutingReport report = ScoutingReport.builder()
            .assignment(assignment)
            .player(player)
            .scout(scout)
            .reportDate(LocalDate.now())
            .accuracyLevel(totalAccuracy)
            .confidenceLevel(calculateConfidenceLevel(scout, totalAccuracy))
            .build();
            
        // Reveal player attributes with accuracy
        revealPlayerAttributes(report, player, totalAccuracy);
        
        // Generate assessments
        generatePlayerAssessment(report, player, totalAccuracy);
        
        // Estimate market information
        generateMarketAssessment(report, player, totalAccuracy);
        
        return reportRepository.save(report);
    }
    
    /**
     * Reveal player attributes with scout accuracy
     */
    private void revealPlayerAttributes(ScoutingReport report, Player player, double accuracy) {
        // Add random variance based on accuracy
        double variance = (1.0 - accuracy) * 10.0; // Up to 10 points variance
        
        report.setRevealedStamina(addScoutingVariance(player.getStamina(), variance));
        report.setRevealedPlaymaking(addScoutingVariance(player.getPlaymaking(), variance));
        report.setRevealedScoring(addScoutingVariance(player.getScoring(), variance));
        report.setRevealedWinger(addScoutingVariance(player.getWinger(), variance));
        report.setRevealedGoalkeeping(addScoutingVariance(player.getGoalkeeping(), variance));
        report.setRevealedPassing(addScoutingVariance(player.getPassing(), variance));
        report.setRevealedDefending(addScoutingVariance(player.getDefending(), variance));
        report.setRevealedSetPieces(addScoutingVariance(player.getSetPieces(), variance));
        
        // Calculate overall and potential ratings
        report.setOverallRating(calculateOverallRating(report));
        report.setPotentialRating(calculatePotentialRating(player, accuracy));
    }
    
    /**
     * Generate player assessment text
     */
    private void generatePlayerAssessment(ScoutingReport report, Player player, double accuracy) {
        // Generate strengths based on highest attributes
        List<String> strengths = identifyPlayerStrengths(report);
        report.setStrengths(String.join(", ", strengths));
        
        // Generate weaknesses based on lowest attributes
        List<String> weaknesses = identifyPlayerWeaknesses(report);
        report.setWeaknesses(String.join(", ", weaknesses));
        
        // Generate personality assessment (with some randomness for realism)
        report.setPersonalityAssessment(generatePersonalityAssessment(player, accuracy));
        
        // Generate recommendation
        report.setRecommendation(calculateRecommendation(report, player));
    }
    
    /**
     * Get player scouting status for a club
     */
    public PlayerScoutingStatus getPlayerScoutingStatus(Long playerId, Long clubId) {
        Player player = playerService.findById(playerId);
        Club club = clubService.findById(clubId);
        
        return scoutingStatusRepository.findByPlayerAndScoutingClub(player, club)
            .orElse(createUnknownScoutingStatus(player, club));
    }
    
    /**
     * Get revealed player attributes for a club
     */
    public PlayerDTO getRevealedPlayerInfo(Long playerId, Long clubId) {
        PlayerScoutingStatus status = getPlayerScoutingStatus(playerId, clubId);
        Player player = playerService.findById(playerId);
        
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setSurname(player.getSurname());
        dto.setBirth(player.getBirth());
        dto.setRole(player.getRole());
        dto.setPreferredFoot(player.getPreferredFoot());
        
        // Set attributes based on scouting level
        switch (status.getScoutingLevel()) {
            case UNKNOWN -> {
                // Only basic info visible
                dto.setStamina(null);
                dto.setPlaymaking(null);
                // ... all attributes hidden
            }
            case BASIC -> {
                // Rough estimates visible
                dto.setStamina(getRoughEstimate(status.getKnownStamina()));
                dto.setPlaymaking(getRoughEstimate(status.getKnownPlaymaking()));
                // ... other attributes with rough estimates
            }
            case DETAILED, COMPREHENSIVE -> {
                // Accurate values visible
                dto.setStamina(status.getKnownStamina());
                dto.setPlaymaking(status.getKnownPlaymaking());
                // ... all known attributes
            }
        }
        
        return dto;
    }
    
    /**
     * Get scouting recommendations for a club
     */
    public List<ScoutingRecommendationDTO> getScoutingRecommendations(Long clubId) {
        Club club = clubService.findById(clubId);
        
        // Get recent reports with high recommendations
        List<ScoutingReport> recommendedReports = reportRepository
            .findRecentRecommendedReports(club, LocalDate.now().minusMonths(3));
            
        return recommendedReports.stream()
            .map(this::convertToRecommendationDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Create watchlist entry from scouting report
     */
    public WatchlistEntry addToWatchlist(Long reportId, String notes) {
        ScoutingReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Scouting report not found"));
            
        return watchlistService.addPlayer(
            report.getPlayer().getId(),
            report.getAssignment().getScout().getClub().getId(),
            notes,
            report.getRecommendation()
        );
    }
    
    private double addScoutingVariance(Double actualValue, double variance) {
        if (actualValue == null) return 0.0;
        
        double randomVariance = RandomUtils.randomValue(-variance, variance);
        double result = actualValue + randomVariance;
        
        return Math.max(0.0, Math.min(99.0, result));
    }
    
    private int calculateScoutingDuration(Scout scout, Player player, ScoutingType type) {
        int baseDays = switch (type) {
            case PLAYER -> 14; // 2 weeks for individual player
            case CLUB -> 30; // 1 month for club overview
            case REGION -> 60; // 2 months for regional scouting
            case OPPOSITION -> 7; // 1 week for opposition analysis
        };
        
        // Adjust based on scout ability (better scouts work faster)
        double abilityMultiplier = 1.0 - (scout.getAbility() / 40.0); // Up to 50% faster
        
        // Adjust based on player difficulty (higher rated players take longer)
        double difficultyMultiplier = 1.0 + (player.getAverage() / 200.0); // Up to 50% longer
        
        return (int) Math.max(3, baseDays * abilityMultiplier * difficultyMultiplier);
    }
    
    private RecommendationLevel calculateRecommendation(ScoutingReport report, Player player) {
        int overallRating = report.getOverallRating();
        int potentialRating = report.getPotentialRating();
        int playerAge = player.getAge();
        
        // Young players with high potential get priority
        if (playerAge < 23 && potentialRating > 85) {
            return RecommendationLevel.PRIORITY;
        }
        
        // High overall rating players
        if (overallRating > 80) {
            return RecommendationLevel.RECOMMEND;
        }
        
        // Decent players worth considering
        if (overallRating > 65) {
            return RecommendationLevel.CONSIDER;
        }
        
        // Players to keep an eye on
        if (overallRating > 50 || (playerAge < 25 && potentialRating > 70)) {
            return RecommendationLevel.MONITOR;
        }
        
        return RecommendationLevel.AVOID;
    }
}
```

### API Endpoints

#### ScoutingController
```java
@RestController
@RequestMapping("/api/scouting")
public class ScoutingController {
    
    @Autowired
    private ScoutingService scoutingService;
    
    @GetMapping("/club/{clubId}/scouts")
    public ResponseEntity<List<ScoutDTO>> getClubScouts(@PathVariable Long clubId) {
        List<Scout> scouts = scoutingService.getClubScouts(clubId);
        return ResponseEntity.ok(scouts.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/assignment/player")
    public ResponseEntity<ScoutingAssignmentDTO> assignPlayerScouting(
            @RequestBody AssignPlayerScoutingRequest request) {
        ScoutingAssignment assignment = scoutingService.assignPlayerScouting(
            request.getScoutId(),
            request.getPlayerId(),
            request.getPriority(),
            request.getInstructions()
        );
        return ResponseEntity.ok(convertToDTO(assignment));
    }
    
    @GetMapping("/club/{clubId}/assignments")
    public ResponseEntity<List<ScoutingAssignmentDTO>> getClubAssignments(
            @PathVariable Long clubId,
            @RequestParam(required = false) AssignmentStatus status) {
        List<ScoutingAssignment> assignments = scoutingService.getClubAssignments(clubId, status);
        return ResponseEntity.ok(assignments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/player/{playerId}/status/{clubId}")
    public ResponseEntity<PlayerScoutingStatusDTO> getPlayerScoutingStatus(
            @PathVariable Long playerId,
            @PathVariable Long clubId) {
        PlayerScoutingStatus status = scoutingService.getPlayerScoutingStatus(playerId, clubId);
        return ResponseEntity.ok(convertToDTO(status));
    }
    
    @GetMapping("/player/{playerId}/revealed/{clubId}")
    public ResponseEntity<PlayerDTO> getRevealedPlayerInfo(
            @PathVariable Long playerId,
            @PathVariable Long clubId) {
        PlayerDTO player = scoutingService.getRevealedPlayerInfo(playerId, clubId);
        return ResponseEntity.ok(player);
    }
    
    @GetMapping("/club/{clubId}/reports")
    public ResponseEntity<Page<ScoutingReportDTO>> getScoutingReports(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RecommendationLevel recommendation) {
        Page<ScoutingReport> reports = scoutingService.getScoutingReports(
            clubId, PageRequest.of(page, size), recommendation);
        return ResponseEntity.ok(reports.map(this::convertToDTO));
    }
    
    @GetMapping("/club/{clubId}/recommendations")
    public ResponseEntity<List<ScoutingRecommendationDTO>> getScoutingRecommendations(
            @PathVariable Long clubId) {
        List<ScoutingRecommendationDTO> recommendations = scoutingService
            .getScoutingRecommendations(clubId);
        return ResponseEntity.ok(recommendations);
    }
    
    @PostMapping("/report/{reportId}/watchlist")
    public ResponseEntity<WatchlistEntryDTO> addToWatchlist(
            @PathVariable Long reportId,
            @RequestBody AddToWatchlistRequest request) {
        WatchlistEntry entry = scoutingService.addToWatchlist(reportId, request.getNotes());
        return ResponseEntity.ok(convertToDTO(entry));
    }
    
    @PostMapping("/assignment/{assignmentId}/cancel")
    public ResponseEntity<Void> cancelAssignment(@PathVariable Long assignmentId) {
        scoutingService.cancelAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }
}
```

### Frontend Implementation

#### ScoutingDashboard Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getClubScouts, getClubAssignments, getScoutingReports } from '../services/api';

const ScoutingDashboard = ({ clubId }) => {
    const [scouts, setScouts] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [reports, setReports] = useState([]);
    const [selectedTab, setSelectedTab] = useState('scouts');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadScoutingData();
    }, [clubId]);

    const loadScoutingData = async () => {
        try {
            const [scoutsResponse, assignmentsResponse, reportsResponse] = await Promise.all([
                getClubScouts(clubId),
                getClubAssignments(clubId),
                getScoutingReports(clubId)
            ]);
            
            setScouts(scoutsResponse.data);
            setAssignments(assignmentsResponse.data);
            setReports(reportsResponse.data.content);
        } catch (error) {
            console.error('Error loading scouting data:', error);
        } finally {
            setLoading(false);
        }
    };

    const getScoutStatusColor = (status) => {
        const colors = {
            ACTIVE: '#4caf50',
            INJURED: '#f44336',
            SUSPENDED: '#ff9800'
        };
        return colors[status] || '#666';
    };

    const getAssignmentStatusIcon = (status) => {
        const icons = {
            ASSIGNED: '📋',
            IN_PROGRESS: '🔍',
            COMPLETED: '✅',
            CANCELLED: '❌'
        };
        return icons[status] || '❓';
    };

    const getRecommendationColor = (recommendation) => {
        const colors = {
            AVOID: '#f44336',
            MONITOR: '#ff9800',
            CONSIDER: '#2196f3',
            RECOMMEND: '#4caf50',
            PRIORITY: '#9c27b0'
        };
        return colors[recommendation] || '#666';
    };

    if (loading) return <div>Loading scouting dashboard...</div>;

    return (
        <div className="scouting-dashboard">
            <div className="dashboard-header">
                <h2>Scouting Network</h2>
                <div className="scouting-stats">
                    <div className="stat">
                        <span>Active Scouts:</span>
                        <span>{scouts.filter(s => s.status === 'ACTIVE').length}</span>
                    </div>
                    <div className="stat">
                        <span>Active Assignments:</span>
                        <span>{assignments.filter(a => a.status === 'IN_PROGRESS').length}</span>
                    </div>
                    <div className="stat">
                        <span>Completed Reports:</span>
                        <span>{reports.length}</span>
                    </div>
                </div>
            </div>

            <div className="dashboard-tabs">
                <button 
                    className={selectedTab === 'scouts' ? 'active' : ''}
                    onClick={() => setSelectedTab('scouts')}
                >
                    Scouts ({scouts.length})
                </button>
                <button 
                    className={selectedTab === 'assignments' ? 'active' : ''}
                    onClick={() => setSelectedTab('assignments')}
                >
                    Assignments ({assignments.length})
                </button>
                <button 
                    className={selectedTab === 'reports' ? 'active' : ''}
                    onClick={() => setSelectedTab('reports')}
                >
                    Reports ({reports.length})
                </button>
            </div>

            {selectedTab === 'scouts' && (
                <div className="scouts-section">
                    <div className="scouts-grid">
                        {scouts.map(scout => (
                            <div key={scout.id} className="scout-card">
                                <div className="scout-header">
                                    <h4>{scout.name} {scout.surname}</h4>
                                    <span 
                                        className="scout-status"
                                        style={{ color: getScoutStatusColor(scout.status) }}
                                    >
                                        {scout.status}
                                    </span>
                                </div>
                                
                                <div className="scout-info">
                                    <div className="info-row">
                                        <span>Ability:</span>
                                        <span>{'⭐'.repeat(Math.ceil(scout.ability / 4))}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Specialization:</span>
                                        <span>{scout.specialization}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Region:</span>
                                        <span>{scout.scoutingRegion?.name || 'Global'}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Experience:</span>
                                        <span>{scout.experience} years</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Salary:</span>
                                        <span>${scout.monthlySalary.toLocaleString()}/month</span>
                                    </div>
                                </div>

                                <div className="scout-assignments">
                                    <strong>Current Assignments:</strong>
                                    <span>{scout.assignments?.filter(a => a.status === 'IN_PROGRESS').length || 0}</span>
                                </div>

                                <div className="scout-actions">
                                    <button className="assign-btn">
                                        New Assignment
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'assignments' && (
                <div className="assignments-section">
                    <div className="assignments-list">
                        {assignments.map(assignment => (
                            <div key={assignment.id} className="assignment-card">
                                <div className="assignment-header">
                                    <span className="assignment-icon">
                                        {getAssignmentStatusIcon(assignment.status)}
                                    </span>
                                    <div className="assignment-info">
                                        <h4>
                                            {assignment.type === 'PLAYER' ? 
                                                `${assignment.targetPlayer.name} ${assignment.targetPlayer.surname}` :
                                                assignment.type
                                            }
                                        </h4>
                                        <span className="assignment-scout">
                                            Scout: {assignment.scout.name} {assignment.scout.surname}
                                        </span>
                                    </div>
                                    <div className="assignment-status">
                                        <span className={`status ${assignment.status.toLowerCase()}`}>
                                            {assignment.status}
                                        </span>
                                    </div>
                                </div>

                                <div className="assignment-details">
                                    <div className="detail-row">
                                        <span>Assigned:</span>
                                        <span>{new Date(assignment.assignedDate).toLocaleDateString()}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span>Expected Completion:</span>
                                        <span>{new Date(assignment.expectedCompletionDate).toLocaleDateString()}</span>
                                    </div>
                                    <div className="detail-row">
                                        <span>Priority:</span>
                                        <span>{'🔥'.repeat(assignment.priority)}</span>
                                    </div>
                                </div>

                                {assignment.instructions && (
                                    <div className="assignment-instructions">
                                        <strong>Instructions:</strong>
                                        <p>{assignment.instructions}</p>
                                    </div>
                                )}

                                <div className="assignment-progress">
                                    <div className="progress-bar">
                                        <div 
                                            className="progress-fill"
                                            style={{ 
                                                width: `${calculateAssignmentProgress(assignment)}%` 
                                            }}
                                        ></div>
                                    </div>
                                    <span>{calculateAssignmentProgress(assignment)}% Complete</span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'reports' && (
                <div className="reports-section">
                    <div className="reports-grid">
                        {reports.map(report => (
                            <div key={report.id} className="report-card">
                                <div className="report-header">
                                    <h4>{report.player.name} {report.player.surname}</h4>
                                    <span 
                                        className="recommendation"
                                        style={{ color: getRecommendationColor(report.recommendation) }}
                                    >
                                        {report.recommendation}
                                    </span>
                                </div>

                                <div className="report-ratings">
                                    <div className="rating">
                                        <span>Overall:</span>
                                        <span className="rating-value">{report.overallRating}/100</span>
                                    </div>
                                    <div className="rating">
                                        <span>Potential:</span>
                                        <span className="rating-value">{report.potentialRating}/100</span>
                                    </div>
                                    <div className="rating">
                                        <span>Confidence:</span>
                                        <span className="rating-value">{report.confidenceLevel}/10</span>
                                    </div>
                                </div>

                                <div className="report-assessment">
                                    <div className="strengths">
                                        <strong>Strengths:</strong>
                                        <p>{report.strengths}</p>
                                    </div>
                                    <div className="weaknesses">
                                        <strong>Weaknesses:</strong>
                                        <p>{report.weaknesses}</p>
                                    </div>
                                </div>

                                <div className="report-market-info">
                                    <div className="market-detail">
                                        <span>Est. Value:</span>
                                        <span>${report.estimatedValue?.toLocaleString()}</span>
                                    </div>
                                    <div className="market-detail">
                                        <span>Est. Wage:</span>
                                        <span>${report.estimatedWage?.toLocaleString()}/week</span>
                                    </div>
                                    <div className="market-detail">
                                        <span>Available:</span>
                                        <span>{report.isAvailableForTransfer ? 'Yes' : 'No'}</span>
                                    </div>
                                </div>

                                <div className="report-actions">
                                    <button className="watchlist-btn">
                                        Add to Watchlist
                                    </button>
                                    <button className="details-btn">
                                        View Details
                                    </button>
                                </div>

                                <div className="report-meta">
                                    <small>
                                        Report by {report.scout.name} {report.scout.surname} - 
                                        {new Date(report.reportDate).toLocaleDateString()}
                                    </small>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

const calculateAssignmentProgress = (assignment) => {
    const now = new Date();
    const assigned = new Date(assignment.assignedDate);
    const expected = new Date(assignment.expectedCompletionDate);
    
    if (assignment.status === 'COMPLETED') return 100;
    if (assignment.status === 'CANCELLED') return 0;
    
    const totalDuration = expected.getTime() - assigned.getTime();
    const elapsed = now.getTime() - assigned.getTime();
    
    return Math.min(100, Math.max(0, (elapsed / totalDuration) * 100));
};

export default ScoutingDashboard;
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class ScoutingServiceTest {
    
    @Mock
    private ScoutRepository scoutRepository;
    
    @Mock
    private ScoutingAssignmentRepository assignmentRepository;
    
    @InjectMocks
    private ScoutingService scoutingService;
    
    @Test
    void testPlayerScoutingAssignment() {
        Scout scout = createTestScout(15); // Ability 15
        Player player = createTestPlayer();
        
        when(scoutRepository.findById(1L)).thenReturn(Optional.of(scout));
        when(assignmentRepository.findActiveAssignmentForPlayer(any(), any()))
            .thenReturn(Optional.empty());
        
        ScoutingAssignment assignment = scoutingService.assignPlayerScouting(
            1L, player.getId(), 3, "Focus on technical skills");
        
        assertThat(assignment.getScout()).isEqualTo(scout);
        assertThat(assignment.getTargetPlayer()).isEqualTo(player);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }
    
    @Test
    void testScoutingReportAccuracy() {
        Scout highAbilityScout = createTestScout(18);
        Scout lowAbilityScout = createTestScout(8);
        Player player = createTestPlayer();
        
        ScoutingReport highAccuracyReport = scoutingService
            .generateScoutingReport(createTestAssignment(highAbilityScout, player));
        ScoutingReport lowAccuracyReport = scoutingService
            .generateScoutingReport(createTestAssignment(lowAbilityScout, player));
        
        assertThat(highAccuracyReport.getAccuracyLevel())
            .isGreaterThan(lowAccuracyReport.getAccuracyLevel());
    }
    
    @Test
    void testPlayerAttributeRevealing() {
        Player player = createTestPlayer();
        player.setScoring(85.0);
        
        PlayerScoutingStatus unknownStatus = createScoutingStatus(ScoutingLevel.UNKNOWN);
        PlayerScoutingStatus detailedStatus = createScoutingStatus(ScoutingLevel.DETAILED);
        
        PlayerDTO unknownDto = scoutingService.getRevealedPlayerInfo(player.getId(), 1L);
        PlayerDTO detailedDto = scoutingService.getRevealedPlayerInfo(player.getId(), 2L);
        
        assertThat(unknownDto.getScoring()).isNull();
        assertThat(detailedDto.getScoring()).isNotNull();
    }
}
```

### Configuration

#### Application Properties
```properties
# Scouting system configuration
fm.scouting.assignment.max-per-scout=3
fm.scouting.report.accuracy.base=0.7
fm.scouting.duration.player.base-days=14
fm.scouting.duration.club.base-days=30
fm.scouting.progress.check.time=09:00
fm.scouting.specialization.bonus=0.1
```

## Implementation Notes

1. **Accuracy System**: Scout ability directly affects the accuracy of revealed player attributes
2. **Progressive Knowledge**: Clubs build knowledge about players over time through multiple scouting reports
3. **Specialization Bonuses**: Scouts perform better when scouting players in their specialization
4. **Market Intelligence**: Scouting reports include transfer availability and estimated costs
5. **AI Integration**: Scouting recommendations can be used by AI clubs for transfer decisions
6. **Performance Impact**: Better scouting leads to better transfer decisions and competitive advantage

## Dependencies

- Player and Club entities for scouting targets
- Staff management system for scout contracts
- Transfer system for market intelligence
- Notification system for scouting completion alerts
- Watchlist system for tracking interesting players
- News system for scouting-related announcements