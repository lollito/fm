package com.lollito.fm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.dto.UpdateWatchlistEntryRequest;
import com.lollito.fm.dto.WatchlistDTO;
import com.lollito.fm.dto.WatchlistEntryDTO;
import com.lollito.fm.dto.WatchlistNotificationDTO;
import com.lollito.fm.dto.WatchlistStatsDTO;
import com.lollito.fm.dto.WatchlistUpdateDTO;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchPlayerStats;
import com.lollito.fm.model.NotificationSeverity;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.UpdateType;
import com.lollito.fm.model.Watchlist;
import com.lollito.fm.model.WatchlistCategory;
import com.lollito.fm.model.WatchlistEntry;
import com.lollito.fm.model.WatchlistNotification;
import com.lollito.fm.model.WatchlistNotificationType;
import com.lollito.fm.model.WatchlistPriority;
import com.lollito.fm.model.WatchlistUpdate;
import com.lollito.fm.repository.MatchPlayerStatsRepository;
import com.lollito.fm.repository.WatchlistEntryRepository;
import com.lollito.fm.repository.WatchlistNotificationRepository;
import com.lollito.fm.repository.WatchlistRepository;
import com.lollito.fm.repository.WatchlistUpdateRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Slf4j
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

    @Autowired
    private ClubService clubService;

    @Autowired
    private MatchPlayerStatsRepository matchPlayerStatsRepository;

    @Value("${fm.watchlist.max-entries.default:50}")
    private Integer defaultMaxEntries;

    @Value("${fm.watchlist.value-change.threshold:0.05}")
    private Double valueChangeThreshold;

    /**
     * Get or create watchlist for club
     */
    public Watchlist getOrCreateWatchlist(Long clubId) {
        Club club = clubService.findById(clubId);

        return watchlistRepository.findByClub(club)
            .orElseGet(() -> createDefaultWatchlist(club));
    }

    private Watchlist createDefaultWatchlist(Club club) {
        Watchlist watchlist = Watchlist.builder()
                .club(club)
                .name("My Watchlist")
                .createdDate(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .maxEntries(defaultMaxEntries)
                .isActive(true)
                .entries(new java.util.ArrayList<>())
                .build();
        return watchlistRepository.save(watchlist);
    }

    /**
     * Add player to watchlist
     */
    public WatchlistEntry addPlayerToWatchlist(Long clubId, Long playerId,
                                             AddToWatchlistRequest request) {
        Watchlist watchlist = getOrCreateWatchlist(clubId);
        Player player = playerService.findOne(playerId);

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
            .priority(request.getPriority() != null ? request.getPriority() : WatchlistPriority.MEDIUM)
            .category(request.getCategory() != null ? request.getCategory() : WatchlistCategory.TARGET)
            .addedValue(calculatePlayerValue(player))
            .currentValue(calculatePlayerValue(player))
            .addedRating(calculatePlayerRating(player))
            .currentRating(calculatePlayerRating(player))
            .notifyOnPerformance(request.getNotifyOnPerformance() != null ? request.getNotifyOnPerformance() : true)
            .notifyOnTransferStatus(request.getNotifyOnTransferStatus() != null ? request.getNotifyOnTransferStatus() : true)
            .notifyOnInjury(request.getNotifyOnInjury() != null ? request.getNotifyOnInjury() : true)
            .notifyOnContractExpiry(request.getNotifyOnContractExpiry() != null ? request.getNotifyOnContractExpiry() : true)
            .notifyOnPriceChange(request.getNotifyOnPriceChange() != null ? request.getNotifyOnPriceChange() : true)
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
        Watchlist watchlist = entry.getWatchlist();
        watchlist.setLastUpdated(LocalDateTime.now());
        watchlistRepository.save(watchlist);
    }

    /**
     * Update watchlist entry notes and settings
     */
    public WatchlistEntry updateWatchlistEntry(Long entryId, UpdateWatchlistEntryRequest request) {
        WatchlistEntry entry = watchlistEntryRepository.findById(entryId)
            .orElseThrow(() -> new EntityNotFoundException("Watchlist entry not found"));

        if (request.getNotes() != null) entry.setNotes(request.getNotes());
        if (request.getPriority() != null) entry.setPriority(request.getPriority());
        if (request.getCategory() != null) entry.setCategory(request.getCategory());
        if (request.getNotifyOnPerformance() != null) entry.setNotifyOnPerformance(request.getNotifyOnPerformance());
        if (request.getNotifyOnTransferStatus() != null) entry.setNotifyOnTransferStatus(request.getNotifyOnTransferStatus());
        if (request.getNotifyOnInjury() != null) entry.setNotifyOnInjury(request.getNotifyOnInjury());
        if (request.getNotifyOnContractExpiry() != null) entry.setNotifyOnContractExpiry(request.getNotifyOnContractExpiry());
        if (request.getNotifyOnPriceChange() != null) entry.setNotifyOnPriceChange(request.getNotifyOnPriceChange());

        return watchlistEntryRepository.save(entry);
    }

    /**
     * Process daily watchlist updates
     */
    @Scheduled(initialDelayString = "${fm.scheduling.watchlist.initial-delay}", fixedRateString = "${fm.scheduling.watchlist.fixed-rate}")
    public void processDailyWatchlistUpdates() {
        log.info("Starting processDailyWatchlistUpdates...");
        List<WatchlistEntry> allEntries = watchlistEntryRepository.findAllActive();

        for (WatchlistEntry entry : allEntries) {
            processWatchlistEntryUpdates(entry);
        }
        log.info("Finished processDailyWatchlistUpdates.");
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
    public void processValueChange(WatchlistEntry entry, BigDecimal newValue) {
        BigDecimal oldValue = entry.getCurrentValue();
        BigDecimal change = newValue.subtract(oldValue);
        double changePercentage = change.divide(oldValue, 4, RoundingMode.HALF_UP)
                                      .multiply(BigDecimal.valueOf(100)).doubleValue();

        // Only notify if change is significant (>5%) and notifications are enabled
        if (Math.abs(changePercentage) > (valueChangeThreshold * 100) && Boolean.TRUE.equals(entry.getNotifyOnPriceChange())) {
            String title = String.format("%s %s - Price %s",
                entry.getPlayer().getName(),
                entry.getPlayer().getSurname(),
                changePercentage > 0 ? "Increased" : "Decreased");

            String message = String.format("Value changed from $%s to $%s (%.1f%%)",
                oldValue.toString(), newValue.toString(), changePercentage);

            NotificationSeverity severity = Math.abs(changePercentage) > 20 ?
                NotificationSeverity.IMPORTANT : NotificationSeverity.INFO;

            createWatchlistNotification(entry, WatchlistNotificationType.PRICE_CHANGE,
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
        if (Boolean.FALSE.equals(entry.getNotifyOnPerformance())) return;

        Player player = entry.getPlayer();

        // Get recent match performances (last 7 days)
        List<MatchPlayerStats> recentStats = matchPlayerStatsRepository
            .findRecentStats(player.getId(), LocalDateTime.now().minusDays(7));

        for (MatchPlayerStats stats : recentStats) {
            // Check if we already notified about this match
            // Ideally we should track which matches we notified about,
            // for now, we can check existing notifications logic or rely on a "last processed match" field.
            // Simplified: check if we have a notification for this match in recent days.
            // Or better: WatchlistUpdate could store match ID if extended, or contextData.

            // Assuming simplified logic: check if stats are notable
            if (isNotablePerformance(stats)) {
                // Check if we already created a notification for this match
                // We can query notificationRepository by type and message content containing match details
                // This is a bit hacky, but consistent with MVP.
                // A better way would be storing lastCheckedMatchId or having a link.
                if (!hasNotifiedAboutMatch(entry, stats.getMatch())) {
                     createPerformanceNotification(entry, stats);
                }
            }
        }
    }

    private boolean hasNotifiedAboutMatch(WatchlistEntry entry, Match match) {
        // Implementation check
        // e.g. check if any notification of type MATCH_PERFORMANCE exists created after match date
        // with contextData containing matchId
        return false; // Placeholder
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

        createWatchlistNotification(entry, WatchlistNotificationType.MATCH_PERFORMANCE,
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

        int totalValueCalc = entries.stream()
            .map(WatchlistEntry::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add).intValue();
        BigDecimal totalValue = BigDecimal.valueOf(totalValueCalc);

        BigDecimal averageValue = totalPlayers > 0 ?
            totalValue.divide(BigDecimal.valueOf(totalPlayers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return WatchlistStatsDTO.builder()
            .totalPlayers(totalPlayers)
            .availablePlayers(availablePlayers)
            .contractsExpiringSoon(contractsExpiringSoon)
            .recentlyPerformed(recentlyPerformed)
            .priceIncreased(priceIncreased)
            .priceDecreased(priceDecreased)
            .averageValue(averageValue)
            .totalValue(totalValue)
            .build();
    }

    public List<WatchlistUpdate> getEntryUpdates(Long entryId) {
        WatchlistEntry entry = watchlistEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entry not found"));
        return updateRepository.findByWatchlistEntryOrderByUpdateDateDesc(entry);
    }

    /**
     * Create watchlist notification
     */
    private void createWatchlistNotification(WatchlistEntry entry, WatchlistNotificationType type,
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
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) return false;

        BigDecimal change = newValue.subtract(oldValue).abs();
        BigDecimal threshold = oldValue.multiply(BigDecimal.valueOf(valueChangeThreshold));

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

    private String getOpponentName(Match match, com.lollito.fm.model.Team playerTeam) {
        // Find if playerTeam is home or away
        // match.getHome().getTeam() ...
        // Simplified:
        if (match.getHome() != null && match.getHome().getTeam() != null && match.getHome().getTeam().getId().equals(playerTeam.getId())) {
            return match.getAway() != null ? match.getAway().getName() : "Unknown";
        } else {
            return match.getHome() != null ? match.getHome().getName() : "Unknown";
        }
    }

    // Helpers for checking changes and converting DTOs

    private BigDecimal calculatePlayerValue(Player player) {
        // Placeholder value calculation
        return BigDecimal.valueOf(player.getAverage() * 1000000L);
    }

    private Double calculatePlayerRating(Player player) {
        return player.getAverage().doubleValue();
    }

    private boolean hasSignificantRatingChange(Double oldRating, Double newRating) {
        if (oldRating == null || newRating == null) return false;
        return Math.abs(newRating - oldRating) >= 1.0;
    }

    private void processRatingChange(WatchlistEntry entry, Double newRating) {
        // Implementation
        entry.setCurrentRating(newRating);
        watchlistEntryRepository.save(entry);
    }

    private boolean hasTransferStatusChanged(WatchlistEntry entry) {
        // Placeholder
        return false;
    }

    private void processTransferStatusChange(WatchlistEntry entry) {
        // Implementation
    }

    private boolean hasInjuryStatusChanged(WatchlistEntry entry) {
        // Placeholder
        return false;
    }

    private void processInjuryStatusChange(WatchlistEntry entry) {
        // Implementation
    }

    private boolean isContractExpiringSoon(WatchlistEntry entry) {
        // Placeholder
        return false;
    }

    private void processContractExpiryNotification(WatchlistEntry entry) {
        // Implementation
    }

    private boolean isPlayerAvailableForTransfer(Player player) {
        return Boolean.TRUE.equals(player.getOnSale());
    }

    private boolean hasRecentGoodPerformance(WatchlistEntry entry) {
        // Simplified check
        return false;
    }

    private WatchlistEntryDTO convertToEntryDTO(WatchlistEntry entry) {
        return WatchlistEntryDTO.builder()
            .id(entry.getId())
            .player(convertToPlayerDTO(entry.getPlayer()))
            .addedDate(entry.getAddedDate())
            .notes(entry.getNotes())
            .priority(entry.getPriority())
            .category(entry.getCategory())
            .addedValue(entry.getAddedValue())
            .currentValue(entry.getCurrentValue())
            .addedRating(entry.getAddedRating())
            .currentRating(entry.getCurrentRating())
            .notifyOnPerformance(entry.getNotifyOnPerformance())
            .notifyOnTransferStatus(entry.getNotifyOnTransferStatus())
            .notifyOnInjury(entry.getNotifyOnInjury())
            .notifyOnContractExpiry(entry.getNotifyOnContractExpiry())
            .notifyOnPriceChange(entry.getNotifyOnPriceChange())
            .totalNotifications(entry.getTotalNotifications())
            .build();
    }

    private WatchlistNotificationDTO convertToNotificationDTO(WatchlistNotification notification) {
        return WatchlistNotificationDTO.builder()
            .id(notification.getId())
            .type(notification.getType())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .detailedMessage(notification.getDetailedMessage())
            .createdDate(notification.getCreatedDate())
            .isRead(notification.getIsRead())
            .isImportant(notification.getIsImportant())
            .severity(notification.getSeverity())
            .build();
    }

    private PlayerDTO convertToPlayerDTO(Player player) {
        if (player == null) return null;
        return PlayerDTO.builder()
            .id(player.getId())
            .name(player.getName())
            .surname(player.getSurname())
            .age(player.getAge())
            .role(player.getRole())
            .salary(player.getSalary())
            // Add other needed fields
            .build();
    }

    public WatchlistUpdateDTO convertToDTO(WatchlistUpdate update) {
        return WatchlistUpdateDTO.builder()
            .id(update.getId())
            .updateType(update.getUpdateType())
            .updateDate(update.getUpdateDate())
            .description(update.getDescription())
            .previousValue(update.getPreviousValue())
            .newValue(update.getNewValue())
            .build();
    }
}
