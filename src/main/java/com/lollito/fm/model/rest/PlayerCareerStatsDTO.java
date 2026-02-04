package com.lollito.fm.model.rest;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

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
