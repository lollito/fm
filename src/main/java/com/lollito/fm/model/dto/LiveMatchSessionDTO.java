package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.lollito.fm.model.MatchIntensity;
import com.lollito.fm.model.MatchPhase;
import com.lollito.fm.model.Match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveMatchSessionDTO {
    private Long id;
    private Match match; // Or MatchDTO if it exists, but task uses Match
    private MatchPhase currentPhase;
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
    private List<MatchEventDTO> events;
    private Integer spectatorCount;
    private String weatherConditions;
    private Double temperature;
    private MatchIntensity intensity;
}
