package com.lollito.fm.model.dto;

import com.lollito.fm.model.MatchIntensity;
import com.lollito.fm.model.MatchPhase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveMatchUpdateDTO {
    private Long matchId;
    private MatchPhase currentPhase;
    private Integer currentMinute;
    private Integer additionalTime;
    private Integer homeScore;
    private Integer awayScore;
    private MatchIntensity intensity;
    private Integer spectatorCount;
}
