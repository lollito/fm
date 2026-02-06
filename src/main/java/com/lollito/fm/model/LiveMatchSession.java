package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "fm_live_match_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveMatchSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed(unique = true)
    private Long matchId;

    private LocalDateTime startTime;

    @Builder.Default
    private Integer currentMinute = 0;

    @Builder.Default
    private Integer homeScore = 0;

    @Builder.Default
    private Integer awayScore = 0;

    private String events; // JSON of List<EventHistoryDTO>

    private String stats; // JSON of StatsDTO

    private String playerStats; // JSON of List<MatchPlayerStatsDTO>

    @Builder.Default
    private Boolean finished = false;
}
