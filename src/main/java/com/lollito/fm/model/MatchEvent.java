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
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "match_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "session_id")
    private String sessionId;

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
