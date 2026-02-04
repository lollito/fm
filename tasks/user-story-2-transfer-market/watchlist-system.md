# Watchlist System Implementation

## Overview
Implement a comprehensive watchlist system that allows managers to follow interesting players and receive notifications about their performances, status changes, and transfer availability.

## Technical Requirements

### Database Schema Changes

#### New Entity: Watchlist
```java
@Entity
@Table(name = "watchlist")
public class Watchlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    private String name; // Default: "My Watchlist"
    private String description;
    
    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL)
    private List<WatchlistEntry> entries = new ArrayList<>();
    
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdated;
    
    private Integer maxEntries; // Configurable limit
    private Boolean isActive;
}
```

#### New Entity: WatchlistEntry
```java
@Entity
@Table(name = "watchlist_entry")
public class WatchlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    private LocalDateTime addedDate;
    private String notes; // User notes about the player
    
    @Enumerated(EnumType.STRING)
    private WatchlistPriority priority; // LOW, MEDIUM, HIGH, URGENT
    
    @Enumerated(EnumType.STRING)
    private WatchlistCategory category; // TARGET, BACKUP, FUTURE, COMPARISON
    
    // Tracking information
    private BigDecimal addedValue; // Player value when added
    private BigDecimal currentValue; // Current player value
    private Double addedRating; // Player rating when added
    private Double currentRating; // Current player rating
    
    // Notification preferences
    private Boolean notifyOnPerformance; // Notify on good/bad performances
    private Boolean notifyOnTransferStatus; // Notify when transfer status changes
    private Boolean notifyOnInjury; // Notify on injuries
    private Boolean notifyOnContractExpiry; // Notify when contract is expiring
    private Boolean notifyOnPriceChange; // Notify on significant price changes
    
    @OneToMany(mappedBy = "watchlistEntry", cascade = CascadeType.ALL)
    private List<WatchlistNotification> notifications = new ArrayList<>();
    
    @OneToMany(mappedBy = "watchlistEntry", cascade = CascadeType.ALL)
    private List<WatchlistUpdate> updates = new ArrayList<>();
    
    private LocalDateTime lastNotificationDate;
    private Integer totalNotifications;
}
```

#### New Entity: WatchlistNotification
```java
@Entity
@Table(name = "watchlist_notification")
public class WatchlistNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_entry_id")
    private WatchlistEntry watchlistEntry;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private String title;
    private String message;
    private String detailedMessage;
    
    private LocalDateTime createdDate;
    private Boolean isRead;
    private Boolean isImportant;
    
    // Context data (JSON)
    private String contextData;
    
    @Enumerated(EnumType.STRING)
    private NotificationSeverity severity; // INFO, WARNING, IMPORTANT, CRITICAL
}
```

#### New Entity: WatchlistUpdate
```java
@Entity
@Table(name = "watchlist_update")
public class WatchlistUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_entry_id")
    private WatchlistEntry watchlistEntry;
    
    @Enumerated(EnumType.STRING)
    private UpdateType updateType;
    
    private LocalDateTime updateDate;
    
    // Before/After values for tracking changes
    private String previousValue;
    private String newValue;
    
    private String description;
    private Boolean triggeredNotification;
}
```

#### Enums to Create
```java
public enum WatchlistPriority {
    LOW("Low Priority", "#4caf50"),
    MEDIUM("Medium Priority", "#ff9800"),
    HIGH("High Priority", "#f44336"),
    URGENT("Urgent", "#9c27b0");
    
    private final String displayName;
    private final String color;
}

public enum WatchlistCategory {
    TARGET("Primary Target"),
    BACKUP("Backup Option"),
    FUTURE("Future Prospect"),
    COMPARISON("Comparison Player"),
    LOAN_TARGET("Loan Target");
    
    private final String displayName;
}

public enum NotificationType {
    PERFORMANCE("Performance Update"),
    TRANSFER_STATUS("Transfer Status Change"),
    INJURY("Injury Update"),
    CONTRACT_EXPIRY("Contract Expiring"),
    PRICE_CHANGE("Price Change"),
    MATCH_PERFORMANCE("Match Performance"),
    AVAILABILITY("Availability Change"),
    COMPETITION("Competition Interest");
    
    private final String displayName;
}

public enum UpdateType {
    VALUE_CHANGE, RATING_CHANGE, TRANSFER_STATUS_CHANGE,
    INJURY_STATUS_CHANGE, CONTRACT_CHANGE, PERFORMANCE_UPDATE
}

public enum NotificationSeverity {
    INFO, WARNING, IMPORTANT, CRITICAL
}
```

### Service Layer Implementation

#### WatchlistService
```java
@Service
public class WatchlistService {
    
    @Autowired
    private WatchlistRepository watchlistRepository;
    
    @Autowired
    private WatchlistEntryRepository watchlistEntryRepository;
    
    @Autowired
    private WatchlistNotificationRepository notificationRepository;
    
    @Autowired
    private WatchlistUpdateRepository updateRepository;
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * Get or create watchlist for club
     */
    public Watchlist getOrCreateWatchlist(Long clubId) {
        Club club = clubService.findById(clubId);
        
        return watchlistRepository.findByClub(club)
            .orElseGet(() -> createDefaultWatchlist(club));
    }
    
    /**
     * Add player to watchlist
     */
    public WatchlistEntry addPlayerToWatchlist(Long clubId, Long playerId, 
                                             AddToWatchlistRequest request) {
        Watchlist watchlist = getOrCreateWatchlist(clubId);
        Player player = playerService.findById(playerId);
        
        // Check if player is already in watchlist
        Optional<WatchlistEntry> existingEntry = watchlistEntryRepository
            .findByWatchlistAndPlayer(watchlist, player);
            
        if (existingEntry.isPresent()) {
            throw new IllegalStateException("Player is already in watchlist");
        }
        
        // Check watchlist capacity
        if (watchlist.getEntries().size() >= watchlist.getMaxEntries()) {
            throw new IllegalStateException("Watchlist is full");
        }
        
        // Create watchlist entry
        WatchlistEntry entry = WatchlistEntry.builder()
            .watchlist(watchlist)
            .player(player)
            .addedDate(LocalDateTime.now())
            .notes(request.getNotes())
            .priority(request.getPriority())
            .category(request.getCategory())
            .addedValue(calculatePlayerValue(player))
            .currentValue(calculatePlayerValue(player))
            .addedRating(calculatePlayerRating(player))
            .currentRating(calculatePlayerRating(player))
            .notifyOnPerformance(request.getNotifyOnPerformance())
            .notifyOnTransferStatus(request.getNotifyOnTransferStatus())
            .notifyOnInjury(request.getNotifyOnInjury())
            .notifyOnContractExpiry(request.getNotifyOnContractExpiry())
            .notifyOnPriceChange(request.getNotifyOnPriceChange())
            .totalNotifications(0)
            .build();
            
        entry = watchlistEntryRepository.save(entry);
        
        // Create initial update record
        createWatchlistUpdate(entry, UpdateType.VALUE_CHANGE, 
                            "Player added to watchlist", null, 
                            entry.getAddedValue().toString());
        
        // Update watchlist timestamp
        watchlist.setLastUpdated(LocalDateTime.now());
        watchlistRepository.save(watchlist);
        
        return entry;
    }
    
    /**
     * Remove player from watchlist
     */
    public void removePlayerFromWatchlist(Long entryId) {
        WatchlistEntry entry = watchlistEntryRepository.findById(entryId)
            .orElseThrow(() -> new EntityNotFoundException("Watchlist entry not found"));
            
        watchlistEntryRepository.delete(entry);
        
        // Update watchlist timestamp
        entry.getWatchlist().setLastUpdated(LocalDateTime.now());
        watchlistRepository.save(entry.getWatchlist());
    }
    
    /**
     * Update watchlist entry notes and settings
     */
    public WatchlistEntry updateWatchlistEntry(Long entryId, UpdateWatchlistEntryRequest request) {
        WatchlistEntry entry = watchlistEntryRepository.findById(entryId)
            .orElseThrow(() -> new EntityNotFoundException("Watchlist entry not found"));
            
        entry.setNotes(request.getNotes());
        entry.setPriority(request.getPriority());
        entry.setCategory(request.getCategory());
        entry.setNotifyOnPerformance(request.getNotifyOnPerformance());
        entry.setNotifyOnTransferStatus(request.getNotifyOnTransferStatus());
        entry.setNotifyOnInjury(request.getNotifyOnInjury());
        entry.setNotifyOnContractExpiry(request.getNotifyOnContractExpiry());
        entry.setNotifyOnPriceChange(request.getNotifyOnPriceChange());
        
        return watchlistEntryRepository.save(entry);
    }
    
    /**
     * Process daily watchlist updates
     */
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    public void processDailyWatchlistUpdates() {
        List<WatchlistEntry> allEntries = watchlistEntryRepository.findAllActive();
        
        for (WatchlistEntry entry : allEntries) {
            processWatchlistEntryUpdates(entry);
        }
    }
    
    /**
     * Process updates for a single watchlist entry
     */
    private void processWatchlistEntryUpdates(WatchlistEntry entry) {
        Player player = entry.getPlayer();
        
        // Check for value changes
        BigDecimal currentValue = calculatePlayerValue(player);
        if (hasSignificantValueChange(entry.getCurrentValue(), currentValue)) {
            processValueChange(entry, currentValue);
        }
        
        // Check for rating changes
        Double currentRating = calculatePlayerRating(player);
        if (hasSignificantRatingChange(entry.getCurrentRating(), currentRating)) {
            processRatingChange(entry, currentRating);
        }
        
        // Check for transfer status changes
        if (hasTransferStatusChanged(entry)) {
            processTransferStatusChange(entry);
        }
        
        // Check for injury status changes
        if (hasInjuryStatusChanged(entry)) {
            processInjuryStatusChange(entry);
        }
        
        // Check for contract expiry
        if (isContractExpiringSoon(entry)) {
            processContractExpiryNotification(entry);
        }
        
        // Check recent match performances
        processRecentPerformances(entry);
    }
    
    /**
     * Process value change notification
     */
    private void processValueChange(WatchlistEntry entry, BigDecimal newValue) {
        BigDecimal oldValue = entry.getCurrentValue();
        BigDecimal change = newValue.subtract(oldValue);
        double changePercentage = change.divide(oldValue, 4, RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        // Only notify if change is significant (>5%) and notifications are enabled
        if (Math.abs(changePercentage) > 5.0 && entry.getNotifyOnPriceChange()) {
            String title = String.format("%s %s - Price %s", 
                entry.getPlayer().getName(), 
                entry.getPlayer().getSurname(),
                changePercentage > 0 ? "Increased" : "Decreased");
                
            String message = String.format("Value changed from $%s to $%s (%.1f%%)",
                oldValue.toString(), newValue.toString(), changePercentage);
                
            NotificationSeverity severity = Math.abs(changePercentage) > 20 ? 
                NotificationSeverity.IMPORTANT : NotificationSeverity.INFO;
                
            createWatchlistNotification(entry, NotificationType.PRICE_CHANGE, 
                                      title, message, severity);
        }
        
        // Update entry with new value
        entry.setCurrentValue(newValue);
        
        // Create update record
        createWatchlistUpdate(entry, UpdateType.VALUE_CHANGE, 
                            "Player value changed", oldValue.toString(), newValue.toString());
        
        watchlistEntryRepository.save(entry);
    }
    
    /**
     * Process match performance notifications
     */
    private void processRecentPerformances(WatchlistEntry entry) {
        if (!entry.getNotifyOnPerformance()) return;
        
        Player player = entry.getPlayer();
        
        // Get recent match performances (last 7 days)
        List<MatchPlayerStats> recentStats = matchStatsService
            .getRecentPlayerStats(player.getId(), 7);
            
        for (MatchPlayerStats stats : recentStats) {
            // Check if we already notified about this match
            if (hasNotifiedAboutMatch(entry, stats.getMatch())) continue;
            
            // Check for notable performances
            if (isNotablePerformance(stats)) {
                createPerformanceNotification(entry, stats);
            }
        }
    }
    
    /**
     * Create performance notification
     */
    private void createPerformanceNotification(WatchlistEntry entry, MatchPlayerStats stats) {
        Player player = entry.getPlayer();
        Match match = stats.getMatch();
        
        String title = String.format("%s %s - %s Performance", 
            player.getName(), player.getSurname(),
            getPerformanceDescription(stats));
            
        String message = String.format("vs %s: %d goals, %d assists, %.1f rating",
            getOpponentName(match, player.getTeam()),
            stats.getGoals(), stats.getAssists(), stats.getRating());
            
        NotificationSeverity severity = stats.getRating() > 8.0 ? 
            NotificationSeverity.IMPORTANT : NotificationSeverity.INFO;
            
        createWatchlistNotification(entry, NotificationType.MATCH_PERFORMANCE, 
                                  title, message, severity);
    }
    
    /**
     * Get watchlist with entries for a club
     */
    public WatchlistDTO getClubWatchlist(Long clubId) {
        Watchlist watchlist = getOrCreateWatchlist(clubId);
        
        List<WatchlistEntryDTO> entries = watchlist.getEntries().stream()
            .map(this::convertToEntryDTO)
            .sorted((a, b) -> b.getPriority().compareTo(a.getPriority()))
            .collect(Collectors.toList());
            
        return WatchlistDTO.builder()
            .id(watchlist.getId())
            .name(watchlist.getName())
            .description(watchlist.getDescription())
            .entries(entries)
            .totalEntries(entries.size())
            .maxEntries(watchlist.getMaxEntries())
            .lastUpdated(watchlist.getLastUpdated())
            .build();
    }
    
    /**
     * Get watchlist notifications for a club
     */
    public List<WatchlistNotificationDTO> getWatchlistNotifications(Long clubId, 
                                                                   Boolean unreadOnly) {
        Watchlist watchlist = getOrCreateWatchlist(clubId);
        
        List<WatchlistNotification> notifications = notificationRepository
            .findByWatchlistOrderByCreatedDateDesc(watchlist, unreadOnly);
            
        return notifications.stream()
            .map(this::convertToNotificationDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark notification as read
     */
    public void markNotificationAsRead(Long notificationId) {
        WatchlistNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
            
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    /**
     * Get watchlist statistics
     */
    public WatchlistStatsDTO getWatchlistStats(Long clubId) {
        Watchlist watchlist = getOrCreateWatchlist(clubId);
        
        List<WatchlistEntry> entries = watchlist.getEntries();
        
        // Calculate statistics
        int totalPlayers = entries.size();
        int availablePlayers = (int) entries.stream()
            .filter(e -> isPlayerAvailableForTransfer(e.getPlayer()))
            .count();
        int contractsExpiringSoon = (int) entries.stream()
            .filter(this::isContractExpiringSoon)
            .count();
        int recentlyPerformed = (int) entries.stream()
            .filter(this::hasRecentGoodPerformance)
            .count();
        int priceIncreased = (int) entries.stream()
            .filter(e -> e.getCurrentValue().compareTo(e.getAddedValue()) > 0)
            .count();
        int priceDecreased = (int) entries.stream()
            .filter(e -> e.getCurrentValue().compareTo(e.getAddedValue()) < 0)
            .count();
            
        return WatchlistStatsDTO.builder()
            .totalPlayers(totalPlayers)
            .availablePlayers(availablePlayers)
            .contractsExpiringSoon(contractsExpiringSoon)
            .recentlyPerformed(recentlyPerformed)
            .priceIncreased(priceIncreased)
            .priceDecreased(priceDecreased)
            .averageValue(calculateAverageValue(entries))
            .totalValue(calculateTotalValue(entries))
            .build();
    }
    
    /**
     * Create watchlist notification
     */
    private void createWatchlistNotification(WatchlistEntry entry, NotificationType type,
                                           String title, String message, 
                                           NotificationSeverity severity) {
        WatchlistNotification notification = WatchlistNotification.builder()
            .watchlistEntry(entry)
            .type(type)
            .title(title)
            .message(message)
            .createdDate(LocalDateTime.now())
            .isRead(false)
            .isImportant(severity == NotificationSeverity.IMPORTANT || 
                        severity == NotificationSeverity.CRITICAL)
            .severity(severity)
            .build();
            
        notificationRepository.save(notification);
        
        // Update entry notification count
        entry.setTotalNotifications(entry.getTotalNotifications() + 1);
        entry.setLastNotificationDate(LocalDateTime.now());
        watchlistEntryRepository.save(entry);
    }
    
    /**
     * Create watchlist update record
     */
    private void createWatchlistUpdate(WatchlistEntry entry, UpdateType type,
                                     String description, String previousValue, 
                                     String newValue) {
        WatchlistUpdate update = WatchlistUpdate.builder()
            .watchlistEntry(entry)
            .updateType(type)
            .updateDate(LocalDateTime.now())
            .description(description)
            .previousValue(previousValue)
            .newValue(newValue)
            .triggeredNotification(false)
            .build();
            
        updateRepository.save(update);
    }
    
    private boolean hasSignificantValueChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null || newValue == null) return false;
        
        BigDecimal change = newValue.subtract(oldValue).abs();
        BigDecimal threshold = oldValue.multiply(BigDecimal.valueOf(0.05)); // 5% threshold
        
        return change.compareTo(threshold) > 0;
    }
    
    private boolean isNotablePerformance(MatchPlayerStats stats) {
        // Notable if: 2+ goals, 2+ assists, 8.0+ rating, or hat-trick
        return stats.getGoals() >= 2 || 
               stats.getAssists() >= 2 || 
               stats.getRating() >= 8.0 ||
               stats.getGoals() >= 3;
    }
    
    private String getPerformanceDescription(MatchPlayerStats stats) {
        if (stats.getGoals() >= 3) return "Hat-trick";
        if (stats.getRating() >= 9.0) return "Outstanding";
        if (stats.getRating() >= 8.0) return "Excellent";
        if (stats.getGoals() >= 2) return "Great";
        return "Good";
    }
}
```

### API Endpoints

#### WatchlistController
```java
@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {
    
    @Autowired
    private WatchlistService watchlistService;
    
    @GetMapping("/club/{clubId}")
    public ResponseEntity<WatchlistDTO> getClubWatchlist(@PathVariable Long clubId) {
        WatchlistDTO watchlist = watchlistService.getClubWatchlist(clubId);
        return ResponseEntity.ok(watchlist);
    }
    
    @PostMapping("/club/{clubId}/player/{playerId}")
    public ResponseEntity<WatchlistEntryDTO> addPlayerToWatchlist(
            @PathVariable Long clubId,
            @PathVariable Long playerId,
            @RequestBody AddToWatchlistRequest request) {
        WatchlistEntry entry = watchlistService.addPlayerToWatchlist(clubId, playerId, request);
        return ResponseEntity.ok(convertToDTO(entry));
    }
    
    @DeleteMapping("/entry/{entryId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long entryId) {
        watchlistService.removePlayerFromWatchlist(entryId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/entry/{entryId}")
    public ResponseEntity<WatchlistEntryDTO> updateWatchlistEntry(
            @PathVariable Long entryId,
            @RequestBody UpdateWatchlistEntryRequest request) {
        WatchlistEntry entry = watchlistService.updateWatchlistEntry(entryId, request);
        return ResponseEntity.ok(convertToDTO(entry));
    }
    
    @GetMapping("/club/{clubId}/notifications")
    public ResponseEntity<List<WatchlistNotificationDTO>> getNotifications(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        List<WatchlistNotificationDTO> notifications = watchlistService
            .getWatchlistNotifications(clubId, unreadOnly);
        return ResponseEntity.ok(notifications);
    }
    
    @PostMapping("/notification/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        watchlistService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/club/{clubId}/stats")
    public ResponseEntity<WatchlistStatsDTO> getWatchlistStats(@PathVariable Long clubId) {
        WatchlistStatsDTO stats = watchlistService.getWatchlistStats(clubId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/entry/{entryId}/updates")
    public ResponseEntity<List<WatchlistUpdateDTO>> getEntryUpdates(@PathVariable Long entryId) {
        List<WatchlistUpdate> updates = watchlistService.getEntryUpdates(entryId);
        return ResponseEntity.ok(updates.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### WatchlistManager Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getClubWatchlist, addPlayerToWatchlist, removeFromWatchlist, 
         getWatchlistNotifications, getWatchlistStats } from '../services/api';

const WatchlistManager = ({ clubId }) => {
    const [watchlist, setWatchlist] = useState(null);
    const [notifications, setNotifications] = useState([]);
    const [stats, setStats] = useState(null);
    const [selectedTab, setSelectedTab] = useState('players');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadWatchlistData();
    }, [clubId]);

    const loadWatchlistData = async () => {
        try {
            const [watchlistResponse, notificationsResponse, statsResponse] = await Promise.all([
                getClubWatchlist(clubId),
                getWatchlistNotifications(clubId),
                getWatchlistStats(clubId)
            ]);
            
            setWatchlist(watchlistResponse.data);
            setNotifications(notificationsResponse.data);
            setStats(statsResponse.data);
        } catch (error) {
            console.error('Error loading watchlist data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRemovePlayer = async (entryId) => {
        try {
            await removeFromWatchlist(entryId);
            loadWatchlistData(); // Refresh data
        } catch (error) {
            console.error('Error removing player from watchlist:', error);
        }
    };

    const getPriorityColor = (priority) => {
        const colors = {
            LOW: '#4caf50',
            MEDIUM: '#ff9800',
            HIGH: '#f44336',
            URGENT: '#9c27b0'
        };
        return colors[priority] || '#666';
    };

    const getCategoryIcon = (category) => {
        const icons = {
            TARGET: '🎯',
            BACKUP: '🔄',
            FUTURE: '🌟',
            COMPARISON: '📊',
            LOAN_TARGET: '📝'
        };
        return icons[category] || '👤';
    };

    const getNotificationIcon = (type) => {
        const icons = {
            PERFORMANCE: '⚽',
            TRANSFER_STATUS: '🔄',
            INJURY: '🏥',
            CONTRACT_EXPIRY: '📅',
            PRICE_CHANGE: '💰',
            MATCH_PERFORMANCE: '🏆',
            AVAILABILITY: '✅',
            COMPETITION: '⚠️'
        };
        return icons[type] || '📢';
    };

    const formatValueChange = (addedValue, currentValue) => {
        const change = currentValue - addedValue;
        const percentage = ((change / addedValue) * 100).toFixed(1);
        const isPositive = change > 0;
        
        return {
            change: Math.abs(change),
            percentage: Math.abs(percentage),
            isPositive,
            color: isPositive ? '#4caf50' : '#f44336',
            arrow: isPositive ? '↗️' : '↘️'
        };
    };

    if (loading) return <div>Loading watchlist...</div>;

    return (
        <div className="watchlist-manager">
            <div className="watchlist-header">
                <h2>Transfer Watchlist</h2>
                <div className="watchlist-summary">
                    <div className="summary-stat">
                        <span>Players:</span>
                        <span>{watchlist?.totalEntries}/{watchlist?.maxEntries}</span>
                    </div>
                    <div className="summary-stat">
                        <span>Available:</span>
                        <span>{stats?.availablePlayers}</span>
                    </div>
                    <div className="summary-stat">
                        <span>Total Value:</span>
                        <span>${stats?.totalValue?.toLocaleString()}</span>
                    </div>
                </div>
            </div>

            <div className="watchlist-tabs">
                <button 
                    className={selectedTab === 'players' ? 'active' : ''}
                    onClick={() => setSelectedTab('players')}
                >
                    Players ({watchlist?.totalEntries})
                </button>
                <button 
                    className={selectedTab === 'notifications' ? 'active' : ''}
                    onClick={() => setSelectedTab('notifications')}
                >
                    Notifications ({notifications.filter(n => !n.isRead).length})
                </button>
                <button 
                    className={selectedTab === 'stats' ? 'active' : ''}
                    onClick={() => setSelectedTab('stats')}
                >
                    Statistics
                </button>
            </div>

            {selectedTab === 'players' && (
                <div className="watchlist-players">
                    <div className="players-grid">
                        {watchlist?.entries?.map(entry => {
                            const valueChange = formatValueChange(entry.addedValue, entry.currentValue);
                            
                            return (
                                <div key={entry.id} className="watchlist-player-card">
                                    <div className="player-header">
                                        <div className="player-name">
                                            <h4>{entry.player.name} {entry.player.surname}</h4>
                                            <span className="player-position">{entry.player.role}</span>
                                        </div>
                                        <div className="player-priority">
                                            <span 
                                                className="priority-badge"
                                                style={{ backgroundColor: getPriorityColor(entry.priority) }}
                                            >
                                                {entry.priority}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="player-info">
                                        <div className="info-row">
                                            <span>Club:</span>
                                            <span>{entry.player.team?.club?.name}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Age:</span>
                                            <span>{entry.player.age}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Category:</span>
                                            <span>
                                                {getCategoryIcon(entry.category)} {entry.category}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="value-tracking">
                                        <div className="value-row">
                                            <span>Added Value:</span>
                                            <span>${entry.addedValue.toLocaleString()}</span>
                                        </div>
                                        <div className="value-row">
                                            <span>Current Value:</span>
                                            <span>${entry.currentValue.toLocaleString()}</span>
                                        </div>
                                        <div className="value-change">
                                            <span 
                                                className="change-indicator"
                                                style={{ color: valueChange.color }}
                                            >
                                                {valueChange.arrow} {valueChange.percentage}%
                                            </span>
                                        </div>
                                    </div>

                                    <div className="rating-tracking">
                                        <div className="rating-row">
                                            <span>Added Rating:</span>
                                            <span>{entry.addedRating?.toFixed(1)}</span>
                                        </div>
                                        <div className="rating-row">
                                            <span>Current Rating:</span>
                                            <span>{entry.currentRating?.toFixed(1)}</span>
                                        </div>
                                    </div>

                                    {entry.notes && (
                                        <div className="player-notes">
                                            <strong>Notes:</strong>
                                            <p>{entry.notes}</p>
                                        </div>
                                    )}

                                    <div className="notification-settings">
                                        <div className="notification-toggles">
                                            {entry.notifyOnPerformance && <span>⚽</span>}
                                            {entry.notifyOnTransferStatus && <span>🔄</span>}
                                            {entry.notifyOnInjury && <span>🏥</span>}
                                            {entry.notifyOnContractExpiry && <span>📅</span>}
                                            {entry.notifyOnPriceChange && <span>💰</span>}
                                        </div>
                                        <span className="notification-count">
                                            {entry.totalNotifications} notifications
                                        </span>
                                    </div>

                                    <div className="player-actions">
                                        <button className="view-btn">
                                            View Player
                                        </button>
                                        <button className="edit-btn">
                                            Edit
                                        </button>
                                        <button 
                                            className="remove-btn"
                                            onClick={() => handleRemovePlayer(entry.id)}
                                        >
                                            Remove
                                        </button>
                                    </div>

                                    <div className="added-date">
                                        <small>
                                            Added: {new Date(entry.addedDate).toLocaleDateString()}
                                        </small>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            {selectedTab === 'notifications' && (
                <div className="watchlist-notifications">
                    <div className="notifications-list">
                        {notifications.map(notification => (
                            <div 
                                key={notification.id} 
                                className={`notification-item ${!notification.isRead ? 'unread' : ''} ${notification.isImportant ? 'important' : ''}`}
                            >
                                <div className="notification-icon">
                                    {getNotificationIcon(notification.type)}
                                </div>
                                <div className="notification-content">
                                    <div className="notification-header">
                                        <h4>{notification.title}</h4>
                                        <span className="notification-time">
                                            {new Date(notification.createdDate).toLocaleDateString()}
                                        </span>
                                    </div>
                                    <p className="notification-message">
                                        {notification.message}
                                    </p>
                                    {notification.detailedMessage && (
                                        <p className="notification-details">
                                            {notification.detailedMessage}
                                        </p>
                                    )}
                                </div>
                                <div className="notification-actions">
                                    {!notification.isRead && (
                                        <button 
                                            className="mark-read-btn"
                                            onClick={() => markAsRead(notification.id)}
                                        >
                                            Mark Read
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'stats' && (
                <div className="watchlist-stats">
                    <div className="stats-grid">
                        <div className="stat-card">
                            <h3>Overview</h3>
                            <div className="stat-item">
                                <span>Total Players:</span>
                                <span>{stats?.totalPlayers}</span>
                            </div>
                            <div className="stat-item">
                                <span>Available for Transfer:</span>
                                <span>{stats?.availablePlayers}</span>
                            </div>
                            <div className="stat-item">
                                <span>Contracts Expiring Soon:</span>
                                <span>{stats?.contractsExpiringSoon}</span>
                            </div>
                            <div className="stat-item">
                                <span>Recently Performed Well:</span>
                                <span>{stats?.recentlyPerformed}</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>Value Tracking</h3>
                            <div className="stat-item">
                                <span>Total Watchlist Value:</span>
                                <span>${stats?.totalValue?.toLocaleString()}</span>
                            </div>
                            <div className="stat-item">
                                <span>Average Player Value:</span>
                                <span>${stats?.averageValue?.toLocaleString()}</span>
                            </div>
                            <div className="stat-item">
                                <span>Values Increased:</span>
                                <span className="positive">{stats?.priceIncreased}</span>
                            </div>
                            <div className="stat-item">
                                <span>Values Decreased:</span>
                                <span className="negative">{stats?.priceDecreased}</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>Activity</h3>
                            <div className="stat-item">
                                <span>Total Notifications:</span>
                                <span>{notifications.length}</span>
                            </div>
                            <div className="stat-item">
                                <span>Unread Notifications:</span>
                                <span>{notifications.filter(n => !n.isRead).length}</span>
                            </div>
                            <div className="stat-item">
                                <span>Important Notifications:</span>
                                <span>{notifications.filter(n => n.isImportant).length}</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default WatchlistManager;
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {
    
    @Mock
    private WatchlistRepository watchlistRepository;
    
    @Mock
    private WatchlistEntryRepository watchlistEntryRepository;
    
    @InjectMocks
    private WatchlistService watchlistService;
    
    @Test
    void testAddPlayerToWatchlist() {
        Club club = createTestClub();
        Player player = createTestPlayer();
        Watchlist watchlist = createTestWatchlist(club);
        
        when(watchlistRepository.findByClub(club)).thenReturn(Optional.of(watchlist));
        when(watchlistEntryRepository.findByWatchlistAndPlayer(watchlist, player))
            .thenReturn(Optional.empty());
        
        AddToWatchlistRequest request = new AddToWatchlistRequest();
        request.setPriority(WatchlistPriority.HIGH);
        request.setCategory(WatchlistCategory.TARGET);
        request.setNotes("Promising young striker");
        
        WatchlistEntry entry = watchlistService.addPlayerToWatchlist(
            club.getId(), player.getId(), request);
        
        assertThat(entry.getPlayer()).isEqualTo(player);
        assertThat(entry.getPriority()).isEqualTo(WatchlistPriority.HIGH);
        assertThat(entry.getNotes()).isEqualTo("Promising young striker");
    }
    
    @Test
    void testValueChangeNotification() {
        WatchlistEntry entry = createTestWatchlistEntry();
        entry.setCurrentValue(BigDecimal.valueOf(1000000));
        entry.setNotifyOnPriceChange(true);
        
        BigDecimal newValue = BigDecimal.valueOf(1200000); // 20% increase
        
        watchlistService.processValueChange(entry, newValue);
        
        verify(notificationRepository).save(any(WatchlistNotification.class));
        assertThat(entry.getCurrentValue()).isEqualTo(newValue);
    }
    
    @Test
    void testWatchlistCapacityLimit() {
        Club club = createTestClub();
        Watchlist watchlist = createTestWatchlist(club);
        watchlist.setMaxEntries(2);
        
        // Add 2 entries to reach capacity
        watchlist.getEntries().add(createTestWatchlistEntry());
        watchlist.getEntries().add(createTestWatchlistEntry());
        
        when(watchlistRepository.findByClub(club)).thenReturn(Optional.of(watchlist));
        
        AddToWatchlistRequest request = new AddToWatchlistRequest();
        
        assertThatThrownBy(() -> 
            watchlistService.addPlayerToWatchlist(club.getId(), 1L, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Watchlist is full");
    }
}
```

### Configuration

#### Application Properties
```properties
# Watchlist system configuration
fm.watchlist.max-entries.default=50
fm.watchlist.notification.batch-size=100
fm.watchlist.update.check.time=08:00
fm.watchlist.value-change.threshold=0.05
fm.watchlist.performance.threshold=8.0
fm.watchlist.contract-expiry.warning-months=6
```

## Implementation Notes

1. **Notification Management**: Implement smart notification batching to avoid spam
2. **Performance Tracking**: Integrate with match statistics for performance notifications
3. **Value Calculation**: Use market-based algorithms for accurate player valuations
4. **Privacy**: Ensure watchlist data is private to each club
5. **Scalability**: Consider pagination for large watchlists
6. **Mobile Support**: Ensure notifications work on mobile devices
7. **Integration**: Connect with scouting system for seamless player discovery

## Dependencies

- Player and Club entities for watchlist targets
- Match statistics system for performance tracking
- Transfer system for availability status
- Contract system for expiry notifications
- Notification system for real-time alerts
- Scouting system integration for discovered players