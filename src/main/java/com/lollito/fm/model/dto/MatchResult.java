package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    private Long matchId;
    private Integer homeScore;
    private Integer awayScore;
    private String homeTeam;
    private String awayTeam;
}
