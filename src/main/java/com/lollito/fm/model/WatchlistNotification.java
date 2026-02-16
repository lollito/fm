package com.lollito.fm.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "watchlist_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_entry_id")
    private WatchlistEntry watchlistEntry;

    @Enumerated(EnumType.STRING)
    private WatchlistNotificationType type;

    private String title;
    private String message;
    private String detailedMessage;

    private LocalDateTime createdDate;
    private Boolean isRead;
    private Boolean isImportant;

    // Context data (JSON)
    private String contextData;

    private Long matchId;

    @Enumerated(EnumType.STRING)
    private NotificationSeverity severity; // INFO, WARNING, IMPORTANT, CRITICAL
}
