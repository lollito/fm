package com.lollito.fm.model.rest;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCareerStatsDTO {
    private Long id;
    private Integer totalMatchesPlayed;
    private Integer totalGoals;
    private Integer totalAssists;
    private Integer totalYellowCards;
    private Integer totalRedCards;
    private Integer totalCleanSheets;
    private Integer leagueTitles;
    private Integer cupTitles;
    private Integer clubsPlayed;
    private BigDecimal totalTransferValue;
    private BigDecimal highestTransferValue;
    private Integer mostGoalsInSeason;
    private Integer mostAssistsInSeason;
    private Double highestSeasonRating;
    private Integer longestGoalStreak;
}
