package com.lollito.fm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "watchlist_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private List<WatchlistNotification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "watchlistEntry", cascade = CascadeType.ALL)
    @Builder.Default
    private List<WatchlistUpdate> updates = new ArrayList<>();

    private LocalDateTime lastNotificationDate;

    @Builder.Default
    private Integer totalNotifications = 0;
}
