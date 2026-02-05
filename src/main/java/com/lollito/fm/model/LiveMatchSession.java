package com.lollito.fm.model;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "live_match_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveMatchSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Enumerated(EnumType.STRING)
    private MatchPhase currentPhase; // PRE_MATCH, FIRST_HALF, HALF_TIME, SECOND_HALF, EXTRA_TIME, PENALTIES, FINISHED

    private Integer currentMinute;
    private Integer additionalTime;

    private LocalDateTime matchStartTime;
    private LocalDateTime halfTimeStart;
    private LocalDateTime secondHalfStart;
    private LocalDateTime matchEndTime;

    private Boolean isPaused;
    private String pauseReason;

    private Integer homeScore;
    private Integer awayScore;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("minute ASC, eventTime ASC")
    @Builder.Default
    private List<MatchEvent> events = new ArrayList<>();

    private Integer spectatorCount; // Live viewers
    private String weatherConditions;
    private Double temperature;

    @Enumerated(EnumType.STRING)
    private MatchIntensity intensity; // LOW, MODERATE, HIGH, EXTREME
}
