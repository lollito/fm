# Youth Academy System Implementation

## Overview
Implement a youth academy system that generates new young talents ("regens") periodically, which can be promoted to the first team.

## Technical Requirements

### Database Schema Changes

#### New Entity: YouthAcademy
```java
@Entity
@Table(name = "youth_academy")
public class YouthAcademy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    private Integer level; // 1-10, affects quality of generated players
    private Integer capacity; // Maximum number of youth players
    private Double reputation; // Affects scouting range and player quality
    
    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL)
    private List<YouthPlayer> youthPlayers = new ArrayList<>();
    
    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL)
    private List<YouthScout> scouts = new ArrayList<>();
    
    private LocalDate lastRegeneration;
    private Integer monthlyBudget; // Budget for academy operations
}
```

#### New Entity: YouthPlayer
```java
@Entity
@Table(name = "youth_player")
public class YouthPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String surname;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birth;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private YouthAcademy academy;
    
    @Enumerated(EnumType.STRING)
    private PlayerRole naturalPosition;
    
    @Enumerated(EnumType.STRING)
    private YouthPotential potential; // POOR, AVERAGE, GOOD, EXCELLENT, WORLD_CLASS
    
    // Current abilities (lower than senior players)
    private Double stamina;
    private Double playmaking;
    private Double scoring;
    private Double winger;
    private Double goalkeeping;
    private Double passing;
    private Double defending;
    private Double setPieces;
    
    // Youth-specific attributes
    private Double determination; // Affects development speed
    private Double professionalism; // Affects training effectiveness
    private Double personality; // Affects team chemistry
    
    @Enumerated(EnumType.STRING)
    private Foot preferredFoot;
    
    private LocalDate joinedAcademy;
    private Boolean readyForPromotion;
    private LocalDate promotionEligibleDate;
    
    @Enumerated(EnumType.STRING)
    private YouthPlayerStatus status; // TRAINING, READY_FOR_PROMOTION, PROMOTED, RELEASED
}
```

#### Enums to Create
```java
public enum YouthPotential {
    POOR(40, 60),        // Max potential 40-60
    AVERAGE(55, 75),     // Max potential 55-75
    GOOD(70, 85),        // Max potential 70-85
    EXCELLENT(80, 95),   // Max potential 80-95
    WORLD_CLASS(90, 99); // Max potential 90-99
    
    private final int minPotential;
    private final int maxPotential;
}

public enum YouthPlayerStatus {
    TRAINING, READY_FOR_PROMOTION, PROMOTED, RELEASED
}
```

#### New Entity: YouthScout
```java
@Entity
@Table(name = "youth_scout")
public class YouthScout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String surname;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private YouthAcademy academy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country scoutingRegion;
    
    private Integer ability; // 1-20, affects quality of found players
    private Integer monthlySalary;
    private LocalDate contractEnd;
    
    @Enumerated(EnumType.STRING)
    private ScoutSpecialization specialization; // GOALKEEPERS, DEFENDERS, MIDFIELDERS, FORWARDS, ALL
}
```

#### Club Entity Updates
```java
// Add to Club.java
@OneToOne(mappedBy = "club", cascade = CascadeType.ALL)
private YouthAcademy youthAcademy;
```

### Service Layer Implementation

#### YouthAcademyService
```java
@Service
public class YouthAcademyService {
    
    @Autowired
    private YouthAcademyRepository youthAcademyRepository;
    
    @Autowired
    private YouthPlayerRepository youthPlayerRepository;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private NameService nameService;
    
    /**
     * Generate new youth players monthly for all academies
     */
    @Scheduled(cron = "0 0 9 1 * *") // First day of month at 9 AM
    public void monthlyYouthGeneration() {
        List<YouthAcademy> academies = youthAcademyRepository.findAll();
        
        for (YouthAcademy academy : academies) {
            if (shouldGenerateYouthPlayers(academy)) {
                generateYouthPlayers(academy);
                academy.setLastRegeneration(LocalDate.now());
                youthAcademyRepository.save(academy);
            }
        }
    }
    
    /**
     * Generate new youth players for an academy
     */
    public List<YouthPlayer> generateYouthPlayers(YouthAcademy academy) {
        List<YouthPlayer> newPlayers = new ArrayList<>();
        
        // Calculate number of players to generate based on academy level and capacity
        int playersToGenerate = calculatePlayersToGenerate(academy);
        
        for (int i = 0; i < playersToGenerate; i++) {
            YouthPlayer player = generateYouthPlayer(academy);
            newPlayers.add(player);
        }
        
        youthPlayerRepository.saveAll(newPlayers);
        return newPlayers;
    }
    
    /**
     * Generate a single youth player
     */
    private YouthPlayer generateYouthPlayer(YouthAcademy academy) {
        // Generate basic info
        String name = nameService.generateRandomName();
        String surname = nameService.generateRandomSurname();
        LocalDate birth = generateYouthBirthDate(); // 16-18 years old
        
        // Determine potential based on academy level and scout quality
        YouthPotential potential = determinePlayerPotential(academy);
        
        // Generate position based on team needs or random
        PlayerRole position = determinePlayerPosition(academy);
        
        YouthPlayer player = YouthPlayer.builder()
            .name(name)
            .surname(surname)
            .birth(birth)
            .academy(academy)
            .naturalPosition(position)
            .potential(potential)
            .preferredFoot(RandomUtils.randomEnum(Foot.class))
            .joinedAcademy(LocalDate.now())
            .status(YouthPlayerStatus.TRAINING)
            .readyForPromotion(false)
            .build();
        
        // Generate initial abilities (lower than potential)
        generateInitialAbilities(player, potential, position);
        
        // Generate personality traits
        generatePersonalityTraits(player);
        
        // Set promotion eligibility (after 6 months minimum)
        player.setPromotionEligibleDate(LocalDate.now().plusMonths(6));
        
        return player;
    }
    
    /**
     * Generate initial abilities for youth player
     */
    private void generateInitialAbilities(YouthPlayer player, YouthPotential potential, 
                                        PlayerRole position) {
        // Base abilities are 30-50% of potential
        double potentialMin = potential.getMinPotential();
        double potentialMax = potential.getMaxPotential();
        double avgPotential = (potentialMin + potentialMax) / 2;
        
        double baseMultiplier = RandomUtils.randomValue(0.3, 0.5);
        
        // Generate abilities based on position (similar to PlayerService)
        switch (position) {
            case GOALKEEPER -> generateGoalkeeperAbilities(player, avgPotential, baseMultiplier);
            case DEFENDER -> generateDefenderAbilities(player, avgPotential, baseMultiplier);
            case WINGBACK -> generateWingbackAbilities(player, avgPotential, baseMultiplier);
            case MIDFIELDER -> generateMidfielderAbilities(player, avgPotential, baseMultiplier);
            case WING -> generateWingerAbilities(player, avgPotential, baseMultiplier);
            case FORWARD -> generateForwardAbilities(player, avgPotential, baseMultiplier);
        }
    }
    
    /**
     * Process monthly development for all youth players
     */
    @Scheduled(cron = "0 0 10 15 * *") // 15th of month at 10 AM
    public void processYouthDevelopment() {
        List<YouthPlayer> youthPlayers = youthPlayerRepository
            .findByStatus(YouthPlayerStatus.TRAINING);
        
        for (YouthPlayer player : youthPlayers) {
            developYouthPlayer(player);
            
            // Check if ready for promotion
            if (isReadyForPromotion(player)) {
                player.setReadyForPromotion(true);
                player.setStatus(YouthPlayerStatus.READY_FOR_PROMOTION);
            }
            
            // Check if should be released (too old or poor development)
            if (shouldReleasePlayer(player)) {
                player.setStatus(YouthPlayerStatus.RELEASED);
            }
        }
        
        youthPlayerRepository.saveAll(youthPlayers);
    }
    
    /**
     * Develop youth player abilities
     */
    private void developYouthPlayer(YouthPlayer player) {
        // Development rate based on age, potential, determination, and academy level
        double developmentRate = calculateDevelopmentRate(player);
        
        // Apply development to all skills
        player.setStamina(Math.min(99.0, player.getStamina() + developmentRate));
        player.setPlaymaking(Math.min(99.0, player.getPlaymaking() + developmentRate));
        player.setScoring(Math.min(99.0, player.getScoring() + developmentRate));
        player.setWinger(Math.min(99.0, player.getWinger() + developmentRate));
        player.setGoalkeeping(Math.min(99.0, player.getGoalkeeping() + developmentRate));
        player.setPassing(Math.min(99.0, player.getPassing() + developmentRate));
        player.setDefending(Math.min(99.0, player.getDefending() + developmentRate));
        player.setSetPieces(Math.min(99.0, player.getSetPieces() + developmentRate));
    }
    
    /**
     * Promote youth player to first team
     */
    public Player promoteYouthPlayer(Long youthPlayerId) {
        YouthPlayer youthPlayer = youthPlayerRepository.findById(youthPlayerId)
            .orElseThrow(() -> new EntityNotFoundException("Youth player not found"));
        
        if (!youthPlayer.getReadyForPromotion()) {
            throw new IllegalStateException("Player not ready for promotion");
        }
        
        // Create senior player from youth player
        Player seniorPlayer = Player.builder()
            .name(youthPlayer.getName())
            .surname(youthPlayer.getSurname())
            .birth(youthPlayer.getBirth())
            .role(youthPlayer.getNaturalPosition())
            .preferredFoot(youthPlayer.getPreferredFoot())
            .stamina(youthPlayer.getStamina())
            .playmaking(youthPlayer.getPlaymaking())
            .scoring(youthPlayer.getScoring())
            .winger(youthPlayer.getWinger())
            .goalkeeping(youthPlayer.getGoalkeeping())
            .passing(youthPlayer.getPassing())
            .defending(youthPlayer.getDefending())
            .setPieces(youthPlayer.getSetPieces())
            .condition(100.0)
            .moral(100.0)
            .onSale(false)
            .team(youthPlayer.getAcademy().getClub().getTeam())
            .salary(calculateInitialSalary(youthPlayer))
            .build();
        
        // Save senior player
        seniorPlayer = playerService.save(seniorPlayer);
        
        // Update youth player status
        youthPlayer.setStatus(YouthPlayerStatus.PROMOTED);
        youthPlayerRepository.save(youthPlayer);
        
        return seniorPlayer;
    }
    
    /**
     * Release youth player from academy
     */
    public void releaseYouthPlayer(Long youthPlayerId, String reason) {
        YouthPlayer youthPlayer = youthPlayerRepository.findById(youthPlayerId)
            .orElseThrow(() -> new EntityNotFoundException("Youth player not found"));
        
        youthPlayer.setStatus(YouthPlayerStatus.RELEASED);
        youthPlayerRepository.save(youthPlayer);
        
        // Create news item about release
        createYouthReleaseNews(youthPlayer, reason);
    }
    
    /**
     * Upgrade youth academy level
     */
    public YouthAcademy upgradeAcademy(Long clubId, AcademyUpgradeRequest request) {
        Club club = clubService.findById(clubId);
        YouthAcademy academy = club.getYouthAcademy();
        
        if (academy == null) {
            academy = createNewAcademy(club);
        }
        
        // Check if club has enough funds
        BigDecimal upgradeCost = calculateUpgradeCost(academy.getLevel(), request.getTargetLevel());
        if (club.getFinance().getBalance().compareTo(upgradeCost) < 0) {
            throw new InsufficientFundsException("Not enough funds for academy upgrade");
        }
        
        // Apply upgrade
        academy.setLevel(request.getTargetLevel());
        academy.setCapacity(request.getNewCapacity());
        academy.setMonthlyBudget(request.getNewBudget());
        
        // Deduct cost from club finances
        club.getFinance().setBalance(club.getFinance().getBalance().subtract(upgradeCost));
        
        return youthAcademyRepository.save(academy);
    }
    
    private boolean isReadyForPromotion(YouthPlayer player) {
        // Must be at least 18 years old
        if (player.getAge() < 18) return false;
        
        // Must have been in academy for at least 6 months
        if (LocalDate.now().isBefore(player.getPromotionEligibleDate())) return false;
        
        // Must have decent abilities (average > 50)
        return player.getAverage() > 50;
    }
    
    private boolean shouldReleasePlayer(YouthPlayer player) {
        // Release if too old (21+) and not good enough
        if (player.getAge() >= 21 && player.getAverage() < 60) return true;
        
        // Release if been in academy too long without progress
        long monthsInAcademy = ChronoUnit.MONTHS.between(
            player.getJoinedAcademy(), LocalDate.now());
        return monthsInAcademy > 36 && player.getAverage() < 55;
    }
}
```

### API Endpoints

#### YouthAcademyController
```java
@RestController
@RequestMapping("/api/youth-academy")
public class YouthAcademyController {
    
    @Autowired
    private YouthAcademyService youthAcademyService;
    
    @GetMapping("/club/{clubId}")
    public ResponseEntity<YouthAcademyDTO> getAcademy(@PathVariable Long clubId) {
        YouthAcademy academy = youthAcademyService.getAcademyByClub(clubId);
        return ResponseEntity.ok(convertToDTO(academy));
    }
    
    @GetMapping("/club/{clubId}/players")
    public ResponseEntity<List<YouthPlayerDTO>> getYouthPlayers(
            @PathVariable Long clubId,
            @RequestParam(required = false) YouthPlayerStatus status) {
        List<YouthPlayer> players = youthAcademyService.getYouthPlayers(clubId, status);
        return ResponseEntity.ok(players.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/player/{playerId}/promote")
    public ResponseEntity<PlayerDTO> promotePlayer(@PathVariable Long playerId) {
        Player promotedPlayer = youthAcademyService.promoteYouthPlayer(playerId);
        return ResponseEntity.ok(convertToDTO(promotedPlayer));
    }
    
    @PostMapping("/player/{playerId}/release")
    public ResponseEntity<Void> releasePlayer(
            @PathVariable Long playerId,
            @RequestBody ReleasePlayerRequest request) {
        youthAcademyService.releaseYouthPlayer(playerId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/club/{clubId}/upgrade")
    public ResponseEntity<YouthAcademyDTO> upgradeAcademy(
            @PathVariable Long clubId,
            @RequestBody AcademyUpgradeRequest request) {
        YouthAcademy academy = youthAcademyService.upgradeAcademy(clubId, request);
        return ResponseEntity.ok(convertToDTO(academy));
    }
    
    @PostMapping("/club/{clubId}/generate-players")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<YouthPlayerDTO>> generatePlayers(@PathVariable Long clubId) {
        List<YouthPlayer> players = youthAcademyService.generatePlayersManually(clubId);
        return ResponseEntity.ok(players.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### YouthAcademy Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getYouthAcademy, getYouthPlayers, promotePlayer, releasePlayer } from '../services/api';

const YouthAcademy = ({ clubId }) => {
    const [academy, setAcademy] = useState(null);
    const [youthPlayers, setYouthPlayers] = useState([]);
    const [selectedTab, setSelectedTab] = useState('players');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadAcademyData();
    }, [clubId]);

    const loadAcademyData = async () => {
        try {
            const [academyResponse, playersResponse] = await Promise.all([
                getYouthAcademy(clubId),
                getYouthPlayers(clubId)
            ]);
            setAcademy(academyResponse.data);
            setYouthPlayers(playersResponse.data);
        } catch (error) {
            console.error('Error loading academy data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handlePromotePlayer = async (playerId) => {
        try {
            await promotePlayer(playerId);
            // Refresh data
            loadAcademyData();
            // Show success notification
        } catch (error) {
            console.error('Error promoting player:', error);
            // Show error notification
        }
    };

    const handleReleasePlayer = async (playerId, reason) => {
        try {
            await releasePlayer(playerId, { reason });
            // Refresh data
            loadAcademyData();
            // Show success notification
        } catch (error) {
            console.error('Error releasing player:', error);
            // Show error notification
        }
    };

    const getPotentialColor = (potential) => {
        const colors = {
            POOR: '#ff4444',
            AVERAGE: '#ffaa00',
            GOOD: '#44aa44',
            EXCELLENT: '#0088ff',
            WORLD_CLASS: '#8844ff'
        };
        return colors[potential] || '#666';
    };

    const getStatusIcon = (status) => {
        const icons = {
            TRAINING: '📚',
            READY_FOR_PROMOTION: '⭐',
            PROMOTED: '✅',
            RELEASED: '❌'
        };
        return icons[status] || '❓';
    };

    if (loading) return <div>Loading youth academy...</div>;

    return (
        <div className="youth-academy">
            <div className="academy-header">
                <h2>Youth Academy</h2>
                <div className="academy-stats">
                    <div className="stat">
                        <label>Level:</label>
                        <span>{academy.level}/10</span>
                    </div>
                    <div className="stat">
                        <label>Capacity:</label>
                        <span>{youthPlayers.length}/{academy.capacity}</span>
                    </div>
                    <div className="stat">
                        <label>Monthly Budget:</label>
                        <span>${academy.monthlyBudget.toLocaleString()}</span>
                    </div>
                    <div className="stat">
                        <label>Reputation:</label>
                        <span>{academy.reputation.toFixed(1)}/10</span>
                    </div>
                </div>
            </div>

            <div className="academy-tabs">
                <button 
                    className={selectedTab === 'players' ? 'active' : ''}
                    onClick={() => setSelectedTab('players')}
                >
                    Youth Players ({youthPlayers.length})
                </button>
                <button 
                    className={selectedTab === 'facilities' ? 'active' : ''}
                    onClick={() => setSelectedTab('facilities')}
                >
                    Facilities
                </button>
                <button 
                    className={selectedTab === 'scouts' ? 'active' : ''}
                    onClick={() => setSelectedTab('scouts')}
                >
                    Scouts
                </button>
            </div>

            {selectedTab === 'players' && (
                <div className="youth-players">
                    <div className="players-grid">
                        {youthPlayers.map(player => (
                            <div key={player.id} className="youth-player-card">
                                <div className="player-header">
                                    <span className="status-icon">
                                        {getStatusIcon(player.status)}
                                    </span>
                                    <h4>{player.name} {player.surname}</h4>
                                    <span className="player-age">({player.age})</span>
                                </div>
                                
                                <div className="player-info">
                                    <div className="position">{player.naturalPosition}</div>
                                    <div 
                                        className="potential"
                                        style={{ color: getPotentialColor(player.potential) }}
                                    >
                                        {player.potential}
                                    </div>
                                    <div className="average">
                                        Avg: {player.average}
                                    </div>
                                </div>

                                <div className="player-attributes">
                                    <div className="attribute">
                                        <span>Stamina:</span>
                                        <span>{Math.round(player.stamina)}</span>
                                    </div>
                                    <div className="attribute">
                                        <span>Playmaking:</span>
                                        <span>{Math.round(player.playmaking)}</span>
                                    </div>
                                    <div className="attribute">
                                        <span>Scoring:</span>
                                        <span>{Math.round(player.scoring)}</span>
                                    </div>
                                    <div className="attribute">
                                        <span>Defending:</span>
                                        <span>{Math.round(player.defending)}</span>
                                    </div>
                                </div>

                                <div className="player-personality">
                                    <div className="trait">
                                        <span>Determination:</span>
                                        <span>{Math.round(player.determination)}</span>
                                    </div>
                                    <div className="trait">
                                        <span>Professionalism:</span>
                                        <span>{Math.round(player.professionalism)}</span>
                                    </div>
                                </div>

                                <div className="player-actions">
                                    {player.status === 'READY_FOR_PROMOTION' && (
                                        <button 
                                            className="promote-btn"
                                            onClick={() => handlePromotePlayer(player.id)}
                                        >
                                            Promote to First Team
                                        </button>
                                    )}
                                    {player.status === 'TRAINING' && (
                                        <button 
                                            className="release-btn"
                                            onClick={() => handleReleasePlayer(player.id, 'Not good enough')}
                                        >
                                            Release
                                        </button>
                                    )}
                                </div>

                                <div className="player-timeline">
                                    <small>
                                        Joined: {new Date(player.joinedAcademy).toLocaleDateString()}
                                    </small>
                                    {player.promotionEligibleDate && (
                                        <small>
                                            Eligible: {new Date(player.promotionEligibleDate).toLocaleDateString()}
                                        </small>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'facilities' && (
                <div className="academy-facilities">
                    <h3>Academy Facilities</h3>
                    <div className="facility-info">
                        <p>Current Level: {academy.level}/10</p>
                        <p>Training Quality Bonus: +{academy.level * 10}%</p>
                        <p>Player Generation Rate: {academy.level} players/month</p>
                        <p>Scouting Range: {academy.level * 100}km radius</p>
                    </div>
                    
                    <div className="upgrade-section">
                        <h4>Upgrade Academy</h4>
                        <p>Upgrading your academy will:</p>
                        <ul>
                            <li>Increase youth player generation rate</li>
                            <li>Improve quality of generated players</li>
                            <li>Expand scouting network</li>
                            <li>Increase academy capacity</li>
                        </ul>
                        <button className="upgrade-btn">
                            Upgrade to Level {academy.level + 1}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default YouthAcademy;
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class YouthAcademyServiceTest {
    
    @Mock
    private YouthPlayerRepository youthPlayerRepository;
    
    @Mock
    private PlayerService playerService;
    
    @InjectMocks
    private YouthAcademyService youthAcademyService;
    
    @Test
    void testYouthPlayerGeneration() {
        YouthAcademy academy = createTestAcademy();
        
        List<YouthPlayer> players = youthAcademyService.generateYouthPlayers(academy);
        
        assertThat(players).isNotEmpty();
        assertThat(players.get(0).getAge()).isBetween(16, 18);
        assertThat(players.get(0).getStatus()).isEqualTo(YouthPlayerStatus.TRAINING);
    }
    
    @Test
    void testPlayerPromotion() {
        YouthPlayer youthPlayer = createReadyYouthPlayer();
        when(youthPlayerRepository.findById(1L)).thenReturn(Optional.of(youthPlayer));
        
        Player promotedPlayer = youthAcademyService.promoteYouthPlayer(1L);
        
        assertThat(promotedPlayer.getName()).isEqualTo(youthPlayer.getName());
        assertThat(promotedPlayer.getAge()).isEqualTo(youthPlayer.getAge());
        verify(playerService).save(any(Player.class));
    }
    
    @Test
    void testYouthDevelopment() {
        YouthPlayer player = createTestYouthPlayer();
        double initialAverage = player.getAverage();
        
        youthAcademyService.developYouthPlayer(player);
        
        assertThat(player.getAverage()).isGreaterThan(initialAverage);
    }
}
```

### Configuration

#### Application Properties
```properties
# Youth academy configuration
fm.youth.generation.day-of-month=1
fm.youth.generation.time=09:00
fm.youth.development.day-of-month=15
fm.youth.development.time=10:00
fm.youth.min-age=16
fm.youth.max-age=18
fm.youth.promotion-min-age=18
fm.youth.release-age=21
fm.youth.min-academy-time-months=6
```

## Implementation Notes

1. **Player Generation**: Balance between quantity and quality based on academy level
2. **Development Rates**: Younger players should develop faster, with diminishing returns as they age
3. **Potential System**: Hidden potential that affects maximum possible ability
4. **Promotion Timing**: Players should be ready for promotion at realistic ages (18+)
5. **Financial Impact**: Academy operations should have ongoing costs
6. **Scouting Integration**: Future enhancement to allow targeted scouting by region/position
7. **Competition**: Other clubs should also have youth academies competing for talent

## Dependencies

- Player service for promotion functionality
- Name generation service for realistic player names
- Club financial system for academy costs
- Notification system for promotion/release alerts
- News system for academy-related news
- Future: Scouting system integration
- Future: Facility management system