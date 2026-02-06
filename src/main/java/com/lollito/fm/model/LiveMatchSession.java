package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fm_live_match_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveMatchSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long matchId;

    private LocalDateTime startTime;

    @Builder.Default
    private Integer currentMinute = 0;

    @Builder.Default
    private Integer homeScore = 0;

    @Builder.Default
    private Integer awayScore = 0;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String events; // JSON of List<EventHistoryDTO>

    @Lob
    @Column(columnDefinition = "TEXT")
    private String stats; // JSON of StatsDTO

    @Lob
    @Column(columnDefinition = "TEXT")
    private String playerStats; // JSON of List<MatchPlayerStatsDTO>

    @Builder.Default
    private Boolean finished = false;
}
