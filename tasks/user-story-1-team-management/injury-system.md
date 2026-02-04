# Injury System Implementation

## Overview
Implement a comprehensive injury system that affects players during matches and training, with recovery times and performance impact.

## Technical Requirements

### Database Schema Changes

#### New Entity: Injury
```java
@Entity
@Table(name = "injury")
public class Injury {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @Enumerated(EnumType.STRING)
    private InjuryType type;
    
    @Enumerated(EnumType.STRING)
    private InjurySeverity severity;
    
    private LocalDate injuryDate;
    private LocalDate expectedRecoveryDate;
    private LocalDate actualRecoveryDate;
    
    @Enumerated(EnumType.STRING)
    private InjuryStatus status; // ACTIVE, RECOVERING, HEALED
    
    private Double performanceImpact; // 0.0 to 1.0 multiplier
    private String description;
}
```

#### Enums to Create
```java
public enum InjuryType {
    MUSCLE_STRAIN, LIGAMENT_DAMAGE, BONE_FRACTURE, 
    CONCUSSION, BRUISE, FATIGUE, OVERUSE
}

public enum InjurySeverity {
    MINOR(1, 7),      // 1-7 days
    MODERATE(7, 21),  // 1-3 weeks  
    MAJOR(21, 84),    // 3-12 weeks
    SEVERE(84, 365);  // 3-12 months
    
    private final int minDays;
    private final int maxDays;
}

public enum InjuryStatus {
    ACTIVE, RECOVERING, HEALED
}
```

#### Player Entity Updates
```java
// Add to Player.java
@OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
private List<Injury> injuries = new ArrayList<>();

@Transient
public boolean isInjured() {
    return injuries.stream()
        .anyMatch(injury -> injury.getStatus() == InjuryStatus.ACTIVE);
}

@Transient
public Double getInjuryPerformanceMultiplier() {
    return injuries.stream()
        .filter(injury -> injury.getStatus() == InjuryStatus.ACTIVE)
        .mapToDouble(Injury::getPerformanceImpact)
        .min()
        .orElse(1.0);
}
```

### Service Layer Implementation

#### InjuryService
```java
@Service
public class InjuryService {
    
    @Autowired
    private InjuryRepository injuryRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    /**
     * Calculate injury probability during match based on player condition,
     * age, previous injuries, and match intensity
     */
    public boolean checkForInjury(Player player, Double matchIntensity) {
        double baseProbability = 0.02; // 2% base chance
        
        // Age factor (older players more prone)
        double ageFactor = player.getAge() > 30 ? 1.5 : 1.0;
        
        // Condition factor (tired players more prone)
        double conditionFactor = player.getCondition() < 50 ? 2.0 : 1.0;
        
        // Previous injury factor
        double injuryHistoryFactor = hasRecentInjuries(player) ? 1.3 : 1.0;
        
        double finalProbability = baseProbability * ageFactor * 
                                conditionFactor * injuryHistoryFactor * 
                                matchIntensity;
        
        return RandomUtils.randomValue(0.0, 1.0) < finalProbability;
    }
    
    /**
     * Create injury for player with random type and severity
     */
    public Injury createInjury(Player player, InjuryContext context) {
        InjuryType type = determineInjuryType(context);
        InjurySeverity severity = determineInjurySeverity(player, type);
        
        int recoveryDays = RandomUtils.randomValue(
            severity.getMinDays(), 
            severity.getMaxDays()
        );
        
        Injury injury = Injury.builder()
            .player(player)
            .type(type)
            .severity(severity)
            .injuryDate(LocalDate.now())
            .expectedRecoveryDate(LocalDate.now().plusDays(recoveryDays))
            .status(InjuryStatus.ACTIVE)
            .performanceImpact(calculatePerformanceImpact(severity))
            .description(generateInjuryDescription(type, severity))
            .build();
            
        return injuryRepository.save(injury);
    }
    
    /**
     * Process daily injury recovery for all injured players
     */
    @Scheduled(cron = "0 0 6 * * *") // Daily at 6 AM
    public void processInjuryRecovery() {
        List<Injury> activeInjuries = injuryRepository
            .findByStatus(InjuryStatus.ACTIVE);
            
        for (Injury injury : activeInjuries) {
            if (LocalDate.now().isAfter(injury.getExpectedRecoveryDate())) {
                // Check for recovery with some randomness
                if (RandomUtils.randomValue(0.0, 1.0) < 0.8) { // 80% chance
                    injury.setStatus(InjuryStatus.HEALED);
                    injury.setActualRecoveryDate(LocalDate.now());
                    injuryRepository.save(injury);
                    
                    // Notify user about recovery
                    createRecoveryNotification(injury);
                }
            }
        }
    }
    
    private Double calculatePerformanceImpact(InjurySeverity severity) {
        return switch (severity) {
            case MINOR -> RandomUtils.randomValue(0.85, 0.95);
            case MODERATE -> RandomUtils.randomValue(0.70, 0.85);
            case MAJOR -> RandomUtils.randomValue(0.50, 0.70);
            case SEVERE -> RandomUtils.randomValue(0.20, 0.50);
        };
    }
}
```

### API Endpoints

#### InjuryController
```java
@RestController
@RequestMapping("/api/injuries")
public class InjuryController {
    
    @Autowired
    private InjuryService injuryService;
    
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<InjuryDTO>> getTeamInjuries(
            @PathVariable Long teamId) {
        List<Injury> injuries = injuryService.getTeamInjuries(teamId);
        return ResponseEntity.ok(injuries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/player/{playerId}/history")
    public ResponseEntity<List<InjuryDTO>> getPlayerInjuryHistory(
            @PathVariable Long playerId) {
        List<Injury> injuries = injuryService.getPlayerInjuryHistory(playerId);
        return ResponseEntity.ok(injuries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/player/{playerId}/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InjuryDTO> createManualInjury(
            @PathVariable Long playerId,
            @RequestBody CreateInjuryRequest request) {
        Injury injury = injuryService.createManualInjury(playerId, request);
        return ResponseEntity.ok(convertToDTO(injury));
    }
}
```

### Frontend Implementation

#### InjuryList Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getTeamInjuries } from '../services/api';

const InjuryList = ({ teamId }) => {
    const [injuries, setInjuries] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadInjuries();
    }, [teamId]);

    const loadInjuries = async () => {
        try {
            const response = await getTeamInjuries(teamId);
            setInjuries(response.data);
        } catch (error) {
            console.error('Error loading injuries:', error);
        } finally {
            setLoading(false);
        }
    };

    const getInjuryIcon = (severity) => {
        const icons = {
            MINOR: '🟡',
            MODERATE: '🟠', 
            MAJOR: '🔴',
            SEVERE: '⚫'
        };
        return icons[severity] || '❓';
    };

    const formatRecoveryTime = (expectedDate) => {
        const days = Math.ceil(
            (new Date(expectedDate) - new Date()) / (1000 * 60 * 60 * 24)
        );
        return days > 0 ? `${days} days` : 'Ready';
    };

    if (loading) return <div>Loading injuries...</div>;

    return (
        <div className="injury-list">
            <h3>Team Injuries</h3>
            {injuries.length === 0 ? (
                <p>No current injuries</p>
            ) : (
                <div className="injury-cards">
                    {injuries.map(injury => (
                        <div key={injury.id} className="injury-card">
                            <div className="injury-header">
                                <span className="injury-icon">
                                    {getInjuryIcon(injury.severity)}
                                </span>
                                <h4>{injury.player.name} {injury.player.surname}</h4>
                            </div>
                            <div className="injury-details">
                                <p><strong>Type:</strong> {injury.type}</p>
                                <p><strong>Severity:</strong> {injury.severity}</p>
                                <p><strong>Recovery:</strong> {formatRecoveryTime(injury.expectedRecoveryDate)}</p>
                                <p><strong>Performance Impact:</strong> 
                                   {Math.round((1 - injury.performanceImpact) * 100)}% reduction
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default InjuryList;
```

### Match Integration

#### Update SimulationMatchService
```java
// Add to SimulationMatchService.java
private void processMatchEvents(Match match, List<Player> homeTeam, List<Player> awayTeam) {
    // Existing match simulation logic...
    
    // Check for injuries during match
    double matchIntensity = calculateMatchIntensity(match);
    
    // Check home team for injuries
    homeTeam.forEach(player -> {
        if (injuryService.checkForInjury(player, matchIntensity)) {
            Injury injury = injuryService.createInjury(player, 
                InjuryContext.MATCH);
            createMatchEvent(match, player, "INJURY", 
                "Player injured: " + injury.getDescription());
        }
    });
    
    // Check away team for injuries
    awayTeam.forEach(player -> {
        if (injuryService.checkForInjury(player, matchIntensity)) {
            Injury injury = injuryService.createInjury(player, 
                InjuryContext.MATCH);
            createMatchEvent(match, player, "INJURY", 
                "Player injured: " + injury.getDescription());
        }
    });
}
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class InjuryServiceTest {
    
    @Mock
    private InjuryRepository injuryRepository;
    
    @InjectMocks
    private InjuryService injuryService;
    
    @Test
    void testInjuryProbabilityCalculation() {
        Player youngPlayer = createTestPlayer(22, 100.0);
        Player oldTiredPlayer = createTestPlayer(35, 30.0);
        
        // Young fit player should have lower injury probability
        boolean youngInjured = false;
        for (int i = 0; i < 1000; i++) {
            if (injuryService.checkForInjury(youngPlayer, 1.0)) {
                youngInjured = true;
                break;
            }
        }
        
        // Old tired player should have higher injury probability
        boolean oldInjured = false;
        for (int i = 0; i < 100; i++) {
            if (injuryService.checkForInjury(oldTiredPlayer, 1.0)) {
                oldInjured = true;
                break;
            }
        }
        
        assertThat(oldInjured).isTrue();
    }
    
    @Test
    void testInjuryRecovery() {
        Injury injury = createTestInjury(InjurySeverity.MINOR);
        injury.setExpectedRecoveryDate(LocalDate.now().minusDays(1));
        
        when(injuryRepository.findByStatus(InjuryStatus.ACTIVE))
            .thenReturn(List.of(injury));
        
        injuryService.processInjuryRecovery();
        
        verify(injuryRepository).save(argThat(i -> 
            i.getStatus() == InjuryStatus.HEALED));
    }
}
```

### Configuration

#### Application Properties
```properties
# Injury system configuration
fm.injury.base-probability=0.02
fm.injury.age-threshold=30
fm.injury.condition-threshold=50
fm.injury.recovery-check-time=06:00
```

## Implementation Notes

1. **Performance Considerations**: Injury checks during match simulation should be optimized to avoid performance impact
2. **Balancing**: Injury probabilities need careful tuning to maintain game balance
3. **User Experience**: Clear visual indicators and notifications for injuries
4. **Data Migration**: Existing players will need default injury history initialization
5. **Localization**: Injury descriptions should support multiple languages
6. **Medical Staff Integration**: Future enhancement to include medical staff affecting recovery times

## Dependencies

- Spring Boot Scheduler for daily recovery processing
- Notification system for injury/recovery alerts
- Match simulation system integration
- Player performance calculation updates