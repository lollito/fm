# Player Career History System Implementation

## Overview
Implement a comprehensive player career history system that tracks detailed statistics, achievements, and performance data across seasons and clubs.

## Technical Requirements

### Database Schema Changes

#### New Entity: PlayerSeasonStats
```java
@Entity
@Table(name = "player_season_stats")
public class PlayerSeasonStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;
    
    // Match statistics
    private Integer matchesPlayed;
    private Integer matchesStarted;
    private Integer minutesPlayed;
    private Integer substitutionsIn;
    private Integer substitutionsOut;
    
    // Performance statistics
    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;
    private Integer cleanSheets; // For goalkeepers and defenders
    
    // Advanced statistics
    private Integer shots;
    private Integer shotsOnTarget;
    private Integer passes;
    private Integer passesCompleted;
    private Integer tackles;
    private Integer interceptions;
    private Integer foulsCommitted;
    private Integer foulsReceived;
    
    // Goalkeeper specific statistics
    private Integer saves;
    private Integer goalsConceded;
    private Integer penaltiesSaved;
    
    // Rating and performance
    private Double averageRating;
    private Double highestRating;
    private Double lowestRating;
    private Integer manOfTheMatchAwards;
    
    // Physical statistics
    private Double averageCondition;
    private Double averageMorale;
    private Integer injuryDays;
    private Integer injuryCount;
}
```

#### New Entity: PlayerCareerStats
```java
@Entity
@Table(name = "player_career_stats")
public class PlayerCareerStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    // Career totals
    private Integer totalMatchesPlayed;
    private Integer totalGoals;
    private Integer totalAssists;
    private Integer totalYellowCards;
    private Integer totalRedCards;
    private Integer totalCleanSheets;
    
    // Career achievements
    private Integer leagueTitles;
    private Integer cupTitles;
    private Integer internationalCaps;
    private Integer internationalGoals;
    
    // Career records
    private Integer longestGoalStreak;
    private Integer mostGoalsInSeason;
    private Integer mostAssistsInSeason;
    private Double highestSeasonRating;
    
    // Career milestones
    private LocalDate firstProfessionalMatch;
    private LocalDate firstGoal;
    private LocalDate milestone100Matches;
    private LocalDate milestone100Goals;
    
    // Transfer history
    private Integer clubsPlayed;
    private BigDecimal totalTransferValue;
    private BigDecimal highestTransferValue;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<PlayerSeasonStats> seasonStats = new ArrayList<>();
}
```

#### New Entity: PlayerAchievement
```java
@Entity
@Table(name = "player_achievement")
public class PlayerAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @Enumerated(EnumType.STRING)
    private AchievementType type;
    
    private String title;
    private String description;
    private LocalDate dateAchieved;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    private String additionalData; // JSON for extra achievement data
}
```

#### New Entity: PlayerTransferHistory
```java
@Entity
@Table(name = "player_transfer_history")
public class PlayerTransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_club_id")
    private Club fromClub;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_club_id")
    private Club toClub;
    
    private LocalDate transferDate;
    private BigDecimal transferFee;
    
    @Enumerated(EnumType.STRING)
    private TransferType transferType; // PURCHASE, LOAN, FREE_TRANSFER, YOUTH_PROMOTION
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
    
    private String transferReason;
    private Integer contractLength; // In years
    private BigDecimal salary;
}
```

#### Enums to Create
```java
public enum AchievementType {
    MILESTONE("Milestone Achievement"),
    PERFORMANCE("Performance Achievement"),
    TEAM_SUCCESS("Team Success"),
    INDIVIDUAL_AWARD("Individual Award"),
    RECORD("Record Achievement");
    
    private final String displayName;
}

public enum TransferType {
    PURCHASE("Purchase"),
    LOAN("Loan"),
    FREE_TRANSFER("Free Transfer"),
    YOUTH_PROMOTION("Youth Promotion"),
    RETIREMENT("Retirement");
    
    private final String displayName;
}
```

#### Player Entity Updates
```java
// Add to Player.java
@OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
private PlayerCareerStats careerStats;

@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
private List<PlayerSeasonStats> seasonStats = new ArrayList<>();

@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
private List<PlayerAchievement> achievements = new ArrayList<>();

@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
private List<PlayerTransferHistory> transferHistory = new ArrayList<>();

@Transient
public PlayerSeasonStats getCurrentSeasonStats() {
    return seasonStats.stream()
        .filter(stats -> stats.getSeason().isCurrent())
        .findFirst()
        .orElse(null);
}

@Transient
public Integer getTotalCareerGoals() {
    return careerStats != null ? careerStats.getTotalGoals() : 0;
}

@Transient
public Integer getTotalCareerMatches() {
    return careerStats != null ? careerStats.getTotalMatchesPlayed() : 0;
}
```

### Service Layer Implementation

#### PlayerHistoryService
```java
@Service
public class PlayerHistoryService {
    
    @Autowired
    private PlayerSeasonStatsRepository playerSeasonStatsRepository;
    
    @Autowired
    private PlayerCareerStatsRepository playerCareerStatsRepository;
    
    @Autowired
    private PlayerAchievementRepository playerAchievementRepository;
    
    @Autowired
    private PlayerTransferHistoryRepository playerTransferHistoryRepository;
    
    @Autowired
    private SeasonService seasonService;
    
    /**
     * Initialize season stats for a player
     */
    public PlayerSeasonStats initializeSeasonStats(Player player, Season season) {
        // Check if stats already exist
        Optional<PlayerSeasonStats> existing = playerSeasonStatsRepository
            .findByPlayerAndSeason(player, season);
            
        if (existing.isPresent()) {
            return existing.get();
        }
        
        PlayerSeasonStats seasonStats = PlayerSeasonStats.builder()
            .player(player)
            .season(season)
            .club(player.getTeam().getClub())
            .league(player.getTeam().getClub().getLeague())
            .matchesPlayed(0)
            .matchesStarted(0)
            .minutesPlayed(0)
            .goals(0)
            .assists(0)
            .yellowCards(0)
            .redCards(0)
            .averageRating(0.0)
            .averageCondition(player.getCondition())
            .averageMorale(player.getMoral())
            .injuryDays(0)
            .injuryCount(0)
            .build();
            
        return playerSeasonStatsRepository.save(seasonStats);
    }
    
    /**
     * Update player statistics after a match
     */
    public void updateMatchStatistics(Player player, MatchPlayerStats matchStats) {
        Season currentSeason = seasonService.getCurrentSeason();
        PlayerSeasonStats seasonStats = getOrCreateSeasonStats(player, currentSeason);
        
        // Update match statistics
        seasonStats.setMatchesPlayed(seasonStats.getMatchesPlayed() + 1);
        if (matchStats.isStarted()) {
            seasonStats.setMatchesStarted(seasonStats.getMatchesStarted() + 1);
        }
        seasonStats.setMinutesPlayed(seasonStats.getMinutesPlayed() + matchStats.getMinutesPlayed());
        
        // Update performance statistics
        seasonStats.setGoals(seasonStats.getGoals() + matchStats.getGoals());
        seasonStats.setAssists(seasonStats.getAssists() + matchStats.getAssists());
        seasonStats.setYellowCards(seasonStats.getYellowCards() + matchStats.getYellowCards());
        seasonStats.setRedCards(seasonStats.getRedCards() + matchStats.getRedCards());
        
        // Update advanced statistics
        if (matchStats.getShots() != null) {
            seasonStats.setShots(seasonStats.getShots() + matchStats.getShots());
        }
        if (matchStats.getShotsOnTarget() != null) {
            seasonStats.setShotsOnTarget(seasonStats.getShotsOnTarget() + matchStats.getShotsOnTarget());
        }
        if (matchStats.getPasses() != null) {
            seasonStats.setPasses(seasonStats.getPasses() + matchStats.getPasses());
        }
        if (matchStats.getPassesCompleted() != null) {
            seasonStats.setPassesCompleted(seasonStats.getPassesCompleted() + matchStats.getPassesCompleted());
        }
        
        // Update goalkeeper statistics
        if (player.getRole() == PlayerRole.GOALKEEPER) {
            if (matchStats.getSaves() != null) {
                seasonStats.setSaves(seasonStats.getSaves() + matchStats.getSaves());
            }
            if (matchStats.getGoalsConceded() != null) {
                seasonStats.setGoalsConceded(seasonStats.getGoalsConceded() + matchStats.getGoalsConceded());
            }
            if (matchStats.getGoalsConceded() == 0) {
                seasonStats.setCleanSheets(seasonStats.getCleanSheets() + 1);
            }
        }
        
        // Update rating
        updatePlayerRating(seasonStats, matchStats.getRating());
        
        playerSeasonStatsRepository.save(seasonStats);
        
        // Update career stats
        updateCareerStats(player);
        
        // Check for achievements
        checkForAchievements(player, seasonStats, matchStats);
    }
    
    /**
     * Update career statistics
     */
    public void updateCareerStats(Player player) {
        PlayerCareerStats careerStats = player.getCareerStats();
        if (careerStats == null) {
            careerStats = new PlayerCareerStats();
            careerStats.setPlayer(player);
            careerStats.setFirstProfessionalMatch(LocalDate.now());
        }
        
        // Calculate totals from all season stats
        List<PlayerSeasonStats> allSeasonStats = playerSeasonStatsRepository.findByPlayer(player);
        
        careerStats.setTotalMatchesPlayed(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getMatchesPlayed).sum()
        );
        careerStats.setTotalGoals(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getGoals).sum()
        );
        careerStats.setTotalAssists(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getAssists).sum()
        );
        careerStats.setTotalYellowCards(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getYellowCards).sum()
        );
        careerStats.setTotalRedCards(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getRedCards).sum()
        );
        careerStats.setTotalCleanSheets(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getCleanSheets).sum()
        );
        
        // Update records
        careerStats.setMostGoalsInSeason(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getGoals).max().orElse(0)
        );
        careerStats.setMostAssistsInSeason(
            allSeasonStats.stream().mapToInt(PlayerSeasonStats::getAssists).max().orElse(0)
        );
        careerStats.setHighestSeasonRating(
            allSeasonStats.stream().mapToDouble(PlayerSeasonStats::getAverageRating).max().orElse(0.0)
        );
        
        // Check for milestones
        checkCareerMilestones(careerStats);
        
        playerCareerStatsRepository.save(careerStats);
    }
    
    /**
     * Record player transfer
     */
    public PlayerTransferHistory recordTransfer(Player player, Club fromClub, Club toClub, 
                                              BigDecimal transferFee, TransferType transferType) {
        Season currentSeason = seasonService.getCurrentSeason();
        
        PlayerTransferHistory transfer = PlayerTransferHistory.builder()
            .player(player)
            .fromClub(fromClub)
            .toClub(toClub)
            .transferDate(LocalDate.now())
            .transferFee(transferFee)
            .transferType(transferType)
            .season(currentSeason)
            .salary(player.getSalary())
            .build();
            
        transfer = playerTransferHistoryRepository.save(transfer);
        
        // Update career stats
        PlayerCareerStats careerStats = player.getCareerStats();
        if (careerStats != null) {
            careerStats.setClubsPlayed(careerStats.getClubsPlayed() + 1);
            careerStats.setTotalTransferValue(
                careerStats.getTotalTransferValue().add(transferFee)
            );
            if (transferFee.compareTo(careerStats.getHighestTransferValue()) > 0) {
                careerStats.setHighestTransferValue(transferFee);
            }
            playerCareerStatsRepository.save(careerStats);
        }
        
        return transfer;
    }
    
    /**
     * Add achievement to player
     */
    public PlayerAchievement addAchievement(Player player, AchievementType type, 
                                          String title, String description) {
        Season currentSeason = seasonService.getCurrentSeason();
        
        PlayerAchievement achievement = PlayerAchievement.builder()
            .player(player)
            .type(type)
            .title(title)
            .description(description)
            .dateAchieved(LocalDate.now())
            .season(currentSeason)
            .club(player.getTeam().getClub())
            .build();
            
        return playerAchievementRepository.save(achievement);
    }
    
    /**
     * Check for achievements after match
     */
    private void checkForAchievements(Player player, PlayerSeasonStats seasonStats, 
                                    MatchPlayerStats matchStats) {
        // First goal achievement
        if (player.getCareerStats().getFirstGoal() == null && matchStats.getGoals() > 0) {
            addAchievement(player, AchievementType.MILESTONE, "First Goal", 
                         "Scored first professional goal");
            player.getCareerStats().setFirstGoal(LocalDate.now());
        }
        
        // Hat-trick achievement
        if (matchStats.getGoals() >= 3) {
            addAchievement(player, AchievementType.PERFORMANCE, "Hat-trick", 
                         "Scored 3 or more goals in a single match");
        }
        
        // Goal milestones
        int totalGoals = player.getCareerStats().getTotalGoals();
        if (totalGoals == 10 || totalGoals == 50 || totalGoals == 100 || totalGoals == 200) {
            addAchievement(player, AchievementType.MILESTONE, 
                         totalGoals + " Career Goals", 
                         "Reached " + totalGoals + " career goals");
        }
        
        // Match milestones
        int totalMatches = player.getCareerStats().getTotalMatchesPlayed();
        if (totalMatches == 50 || totalMatches == 100 || totalMatches == 200 || totalMatches == 500) {
            addAchievement(player, AchievementType.MILESTONE, 
                         totalMatches + " Career Matches", 
                         "Played " + totalMatches + " professional matches");
            
            if (totalMatches == 100 && player.getCareerStats().getMilestone100Matches() == null) {
                player.getCareerStats().setMilestone100Matches(LocalDate.now());
            }
        }
        
        // Season achievements
        if (seasonStats.getGoals() == 20 || seasonStats.getGoals() == 30) {
            addAchievement(player, AchievementType.PERFORMANCE, 
                         seasonStats.getGoals() + " Goals in Season", 
                         "Scored " + seasonStats.getGoals() + " goals in a single season");
        }
        
        // Clean sheet achievements for goalkeepers
        if (player.getRole() == PlayerRole.GOALKEEPER && seasonStats.getCleanSheets() >= 15) {
            addAchievement(player, AchievementType.PERFORMANCE, 
                         "Clean Sheet Specialist", 
                         "Kept " + seasonStats.getCleanSheets() + " clean sheets in a season");
        }
    }
    
    /**
     * Get player's complete history
     */
    public PlayerHistoryDTO getPlayerHistory(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new EntityNotFoundException("Player not found"));
            
        List<PlayerSeasonStats> seasonStats = playerSeasonStatsRepository.findByPlayerOrderBySeasonDesc(player);
        List<PlayerAchievement> achievements = playerAchievementRepository.findByPlayerOrderByDateAchievedDesc(player);
        List<PlayerTransferHistory> transfers = playerTransferHistoryRepository.findByPlayerOrderByTransferDateDesc(player);
        
        return PlayerHistoryDTO.builder()
            .player(convertToDTO(player))
            .careerStats(convertToDTO(player.getCareerStats()))
            .seasonStats(seasonStats.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .achievements(achievements.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .transferHistory(transfers.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .build();
    }
    
    /**
     * Get season statistics for all players in a league
     */
    public List<PlayerSeasonStats> getLeagueTopScorers(Long leagueId, Season season, int limit) {
        return playerSeasonStatsRepository.findTopScorersByLeagueAndSeason(leagueId, season, 
                                                                          PageRequest.of(0, limit));
    }
    
    /**
     * Get season statistics for all players in a league
     */
    public List<PlayerSeasonStats> getLeagueTopAssists(Long leagueId, Season season, int limit) {
        return playerSeasonStatsRepository.findTopAssistsByLeagueAndSeason(leagueId, season, 
                                                                          PageRequest.of(0, limit));
    }
    
    private void updatePlayerRating(PlayerSeasonStats seasonStats, Double matchRating) {
        if (matchRating == null) return;
        
        int matchesPlayed = seasonStats.getMatchesPlayed();
        double currentAverage = seasonStats.getAverageRating();
        
        // Calculate new average rating
        double newAverage = ((currentAverage * (matchesPlayed - 1)) + matchRating) / matchesPlayed;
        seasonStats.setAverageRating(newAverage);
        
        // Update highest/lowest ratings
        if (seasonStats.getHighestRating() == null || matchRating > seasonStats.getHighestRating()) {
            seasonStats.setHighestRating(matchRating);
        }
        if (seasonStats.getLowestRating() == null || matchRating < seasonStats.getLowestRating()) {
            seasonStats.setLowestRating(matchRating);
        }
    }
    
    private void checkCareerMilestones(PlayerCareerStats careerStats) {
        // 100 goals milestone
        if (careerStats.getTotalGoals() >= 100 && careerStats.getMilestone100Goals() == null) {
            careerStats.setMilestone100Goals(LocalDate.now());
        }
        
        // 100 matches milestone
        if (careerStats.getTotalMatchesPlayed() >= 100 && careerStats.getMilestone100Matches() == null) {
            careerStats.setMilestone100Matches(LocalDate.now());
        }
    }
}
```

### API Endpoints

#### PlayerHistoryController
```java
@RestController
@RequestMapping("/api/player-history")
public class PlayerHistoryController {
    
    @Autowired
    private PlayerHistoryService playerHistoryService;
    
    @GetMapping("/player/{playerId}")
    public ResponseEntity<PlayerHistoryDTO> getPlayerHistory(@PathVariable Long playerId) {
        PlayerHistoryDTO history = playerHistoryService.getPlayerHistory(playerId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/player/{playerId}/season/{seasonId}")
    public ResponseEntity<PlayerSeasonStatsDTO> getPlayerSeasonStats(
            @PathVariable Long playerId, 
            @PathVariable Long seasonId) {
        PlayerSeasonStats stats = playerHistoryService.getPlayerSeasonStats(playerId, seasonId);
        return ResponseEntity.ok(convertToDTO(stats));
    }
    
    @GetMapping("/player/{playerId}/achievements")
    public ResponseEntity<List<PlayerAchievementDTO>> getPlayerAchievements(@PathVariable Long playerId) {
        List<PlayerAchievement> achievements = playerHistoryService.getPlayerAchievements(playerId);
        return ResponseEntity.ok(achievements.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/player/{playerId}/transfers")
    public ResponseEntity<List<PlayerTransferHistoryDTO>> getPlayerTransfers(@PathVariable Long playerId) {
        List<PlayerTransferHistory> transfers = playerHistoryService.getPlayerTransfers(playerId);
        return ResponseEntity.ok(transfers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/league/{leagueId}/top-scorers")
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getTopScorers(
            @PathVariable Long leagueId,
            @RequestParam(required = false) Long seasonId,
            @RequestParam(defaultValue = "20") int limit) {
        Season season = seasonId != null ? seasonService.findById(seasonId) : seasonService.getCurrentSeason();
        List<PlayerSeasonStats> topScorers = playerHistoryService.getLeagueTopScorers(leagueId, season, limit);
        return ResponseEntity.ok(topScorers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/league/{leagueId}/top-assists")
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getTopAssists(
            @PathVariable Long leagueId,
            @RequestParam(required = false) Long seasonId,
            @RequestParam(defaultValue = "20") int limit) {
        Season season = seasonId != null ? seasonService.findById(seasonId) : seasonService.getCurrentSeason();
        List<PlayerSeasonStats> topAssists = playerHistoryService.getLeagueTopAssists(leagueId, season, limit);
        return ResponseEntity.ok(topAssists.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/player/{playerId}/achievement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerAchievementDTO> addAchievement(
            @PathVariable Long playerId,
            @RequestBody AddAchievementRequest request) {
        PlayerAchievement achievement = playerHistoryService.addAchievement(
            playerId, request.getType(), request.getTitle(), request.getDescription());
        return ResponseEntity.ok(convertToDTO(achievement));
    }
}
```

### Frontend Implementation

#### PlayerHistory Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getPlayerHistory } from '../services/api';
import { Line, Bar } from 'react-chartjs-2';

const PlayerHistory = ({ playerId }) => {
    const [playerHistory, setPlayerHistory] = useState(null);
    const [selectedTab, setSelectedTab] = useState('overview');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadPlayerHistory();
    }, [playerId]);

    const loadPlayerHistory = async () => {
        try {
            const response = await getPlayerHistory(playerId);
            setPlayerHistory(response.data);
        } catch (error) {
            console.error('Error loading player history:', error);
        } finally {
            setLoading(false);
        }
    };

    const getGoalsPerSeasonData = () => {
        if (!playerHistory?.seasonStats) return null;
        
        const sortedStats = [...playerHistory.seasonStats].reverse();
        
        return {
            labels: sortedStats.map(stat => `${stat.season.startYear}/${stat.season.endYear}`),
            datasets: [{
                label: 'Goals',
                data: sortedStats.map(stat => stat.goals),
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                tension: 0.1
            }]
        };
    };

    const getPerformanceData = () => {
        if (!playerHistory?.seasonStats) return null;
        
        const sortedStats = [...playerHistory.seasonStats].reverse();
        
        return {
            labels: sortedStats.map(stat => `${stat.season.startYear}/${stat.season.endYear}`),
            datasets: [
                {
                    label: 'Goals',
                    data: sortedStats.map(stat => stat.goals),
                    backgroundColor: 'rgba(255, 99, 132, 0.5)',
                },
                {
                    label: 'Assists',
                    data: sortedStats.map(stat => stat.assists),
                    backgroundColor: 'rgba(54, 162, 235, 0.5)',
                },
                {
                    label: 'Matches',
                    data: sortedStats.map(stat => stat.matchesPlayed),
                    backgroundColor: 'rgba(255, 206, 86, 0.5)',
                }
            ]
        };
    };

    const getAchievementIcon = (type) => {
        const icons = {
            MILESTONE: '🏆',
            PERFORMANCE: '⭐',
            TEAM_SUCCESS: '🥇',
            INDIVIDUAL_AWARD: '🏅',
            RECORD: '📈'
        };
        return icons[type] || '🎯';
    };

    const getTransferTypeColor = (type) => {
        const colors = {
            PURCHASE: '#4caf50',
            LOAN: '#ff9800',
            FREE_TRANSFER: '#2196f3',
            YOUTH_PROMOTION: '#9c27b0',
            RETIREMENT: '#f44336'
        };
        return colors[type] || '#666';
    };

    if (loading) return <div>Loading player history...</div>;
    if (!playerHistory) return <div>Player history not found</div>;

    const { player, careerStats, seasonStats, achievements, transferHistory } = playerHistory;

    return (
        <div className="player-history">
            <div className="player-header">
                <h2>{player.name} {player.surname}</h2>
                <div className="player-basic-info">
                    <span>Age: {player.age}</span>
                    <span>Position: {player.role}</span>
                    <span>Current Club: {player.team?.club?.name}</span>
                </div>
            </div>

            <div className="career-overview">
                <div className="career-stats-grid">
                    <div className="stat-card">
                        <h3>Career Matches</h3>
                        <span className="stat-value">{careerStats.totalMatchesPlayed}</span>
                    </div>
                    <div className="stat-card">
                        <h3>Career Goals</h3>
                        <span className="stat-value">{careerStats.totalGoals}</span>
                    </div>
                    <div className="stat-card">
                        <h3>Career Assists</h3>
                        <span className="stat-value">{careerStats.totalAssists}</span>
                    </div>
                    <div className="stat-card">
                        <h3>Clubs Played</h3>
                        <span className="stat-value">{careerStats.clubsPlayed}</span>
                    </div>
                    <div className="stat-card">
                        <h3>League Titles</h3>
                        <span className="stat-value">{careerStats.leagueTitles}</span>
                    </div>
                    <div className="stat-card">
                        <h3>Highest Transfer</h3>
                        <span className="stat-value">${careerStats.highestTransferValue?.toLocaleString()}</span>
                    </div>
                </div>
            </div>

            <div className="history-tabs">
                <button 
                    className={selectedTab === 'overview' ? 'active' : ''}
                    onClick={() => setSelectedTab('overview')}
                >
                    Overview
                </button>
                <button 
                    className={selectedTab === 'seasons' ? 'active' : ''}
                    onClick={() => setSelectedTab('seasons')}
                >
                    Season Stats
                </button>
                <button 
                    className={selectedTab === 'achievements' ? 'active' : ''}
                    onClick={() => setSelectedTab('achievements')}
                >
                    Achievements ({achievements.length})
                </button>
                <button 
                    className={selectedTab === 'transfers' ? 'active' : ''}
                    onClick={() => setSelectedTab('transfers')}
                >
                    Transfer History
                </button>
            </div>

            {selectedTab === 'overview' && (
                <div className="overview-tab">
                    <div className="charts-section">
                        <div className="chart-container">
                            <h3>Goals per Season</h3>
                            {getGoalsPerSeasonData() && (
                                <Line data={getGoalsPerSeasonData()} options={{
                                    responsive: true,
                                    plugins: {
                                        legend: { position: 'top' }
                                    }
                                }} />
                            )}
                        </div>
                        
                        <div className="chart-container">
                            <h3>Performance Overview</h3>
                            {getPerformanceData() && (
                                <Bar data={getPerformanceData()} options={{
                                    responsive: true,
                                    plugins: {
                                        legend: { position: 'top' }
                                    }
                                }} />
                            )}
                        </div>
                    </div>

                    <div className="career-records">
                        <h3>Career Records</h3>
                        <div className="records-grid">
                            <div className="record-item">
                                <span>Most Goals in Season:</span>
                                <span>{careerStats.mostGoalsInSeason}</span>
                            </div>
                            <div className="record-item">
                                <span>Most Assists in Season:</span>
                                <span>{careerStats.mostAssistsInSeason}</span>
                            </div>
                            <div className="record-item">
                                <span>Highest Season Rating:</span>
                                <span>{careerStats.highestSeasonRating?.toFixed(2)}</span>
                            </div>
                            <div className="record-item">
                                <span>Longest Goal Streak:</span>
                                <span>{careerStats.longestGoalStreak} matches</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {selectedTab === 'seasons' && (
                <div className="seasons-tab">
                    <div className="seasons-table">
                        <div className="table-header">
                            <span>Season</span>
                            <span>Club</span>
                            <span>League</span>
                            <span>Matches</span>
                            <span>Goals</span>
                            <span>Assists</span>
                            <span>Rating</span>
                            <span>Cards</span>
                        </div>
                        {seasonStats.map(stat => (
                            <div key={stat.id} className="table-row">
                                <span>{stat.season.startYear}/{stat.season.endYear}</span>
                                <span>{stat.club.name}</span>
                                <span>{stat.league.name}</span>
                                <span>{stat.matchesPlayed}</span>
                                <span>{stat.goals}</span>
                                <span>{stat.assists}</span>
                                <span>{stat.averageRating?.toFixed(2)}</span>
                                <span>
                                    <span className="yellow-card">🟨{stat.yellowCards}</span>
                                    <span className="red-card">🟥{stat.redCards}</span>
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'achievements' && (
                <div className="achievements-tab">
                    <div className="achievements-grid">
                        {achievements.map(achievement => (
                            <div key={achievement.id} className="achievement-card">
                                <div className="achievement-icon">
                                    {getAchievementIcon(achievement.type)}
                                </div>
                                <div className="achievement-info">
                                    <h4>{achievement.title}</h4>
                                    <p>{achievement.description}</p>
                                    <div className="achievement-details">
                                        <span>{new Date(achievement.dateAchieved).toLocaleDateString()}</span>
                                        <span>{achievement.club?.name}</span>
                                        <span>{achievement.season?.startYear}/{achievement.season?.endYear}</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'transfers' && (
                <div className="transfers-tab">
                    <div className="transfers-timeline">
                        {transferHistory.map(transfer => (
                            <div key={transfer.id} className="transfer-item">
                                <div className="transfer-date">
                                    {new Date(transfer.transferDate).toLocaleDateString()}
                                </div>
                                <div className="transfer-details">
                                    <div className="transfer-clubs">
                                        <span className="from-club">{transfer.fromClub?.name || 'Youth Academy'}</span>
                                        <span className="transfer-arrow">→</span>
                                        <span className="to-club">{transfer.toClub.name}</span>
                                    </div>
                                    <div className="transfer-info">
                                        <span 
                                            className="transfer-type"
                                            style={{ color: getTransferTypeColor(transfer.transferType) }}
                                        >
                                            {transfer.transferType.replace('_', ' ')}
                                        </span>
                                        {transfer.transferFee > 0 && (
                                            <span className="transfer-fee">
                                                ${transfer.transferFee.toLocaleString()}
                                            </span>
                                        )}
                                    </div>
                                    <div className="transfer-season">
                                        Season: {transfer.season.startYear}/{transfer.season.endYear}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default PlayerHistory;
```

### Integration with Match System

#### Update MatchService to record player statistics
```java
// In MatchService.java or SimulationMatchService.java
@Autowired
private PlayerHistoryService playerHistoryService;

private void processMatchResult(Match match) {
    // Existing match processing logic...
    
    // Record player statistics for both teams
    List<MatchPlayerStats> homePlayerStats = getMatchPlayerStats(match, match.getHomeTeam());
    List<MatchPlayerStats> awayPlayerStats = getMatchPlayerStats(match, match.getAwayTeam());
    
    homePlayerStats.forEach(stats -> 
        playerHistoryService.updateMatchStatistics(stats.getPlayer(), stats));
    awayPlayerStats.forEach(stats -> 
        playerHistoryService.updateMatchStatistics(stats.getPlayer(), stats));
}
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class PlayerHistoryServiceTest {
    
    @Mock
    private PlayerSeasonStatsRepository playerSeasonStatsRepository;
    
    @Mock
    private PlayerCareerStatsRepository playerCareerStatsRepository;
    
    @InjectMocks
    private PlayerHistoryService playerHistoryService;
    
    @Test
    void testSeasonStatsInitialization() {
        Player player = createTestPlayer();
        Season season = createTestSeason();
        
        PlayerSeasonStats stats = playerHistoryService.initializeSeasonStats(player, season);
        
        assertThat(stats.getPlayer()).isEqualTo(player);
        assertThat(stats.getSeason()).isEqualTo(season);
        assertThat(stats.getGoals()).isEqualTo(0);
        assertThat(stats.getMatchesPlayed()).isEqualTo(0);
    }
    
    @Test
    void testMatchStatisticsUpdate() {
        Player player = createTestPlayer();
        MatchPlayerStats matchStats = createTestMatchStats(2, 1); // 2 goals, 1 assist
        
        playerHistoryService.updateMatchStatistics(player, matchStats);
        
        verify(playerSeasonStatsRepository).save(any(PlayerSeasonStats.class));
    }
    
    @Test
    void testAchievementCreation() {
        Player player = createTestPlayer();
        
        PlayerAchievement achievement = playerHistoryService.addAchievement(
            player, AchievementType.MILESTONE, "First Goal", "Scored first goal");
        
        assertThat(achievement.getPlayer()).isEqualTo(player);
        assertThat(achievement.getTitle()).isEqualTo("First Goal");
    }
}
```

### Configuration

#### Application Properties
```properties
# Player history configuration
fm.player-history.achievement-check.enabled=true
fm.player-history.milestone-goals=10,50,100,200,500
fm.player-history.milestone-matches=50,100,200,500,1000
fm.player-history.season-stats.auto-create=true
```

## Implementation Notes

1. **Performance**: Consider indexing frequently queried fields (player_id, season_id, league_id)
2. **Data Migration**: Existing players will need historical data initialization
3. **Statistics Accuracy**: Ensure match simulation provides accurate player statistics
4. **Achievement System**: Balance between meaningful achievements and spam
5. **Career Progression**: Track skill development over time
6. **International Career**: Future enhancement for national team statistics
7. **Comparison Tools**: Add player comparison functionality

## Dependencies

- Match simulation system for generating player statistics
- Season management system for organizing statistics
- Transfer system for recording player movements
- Achievement notification system
- Player rating system for match performance
- News system for achievement announcements