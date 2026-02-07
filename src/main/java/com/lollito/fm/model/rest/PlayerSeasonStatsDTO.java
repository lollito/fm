package com.lollito.fm.model.rest;

import com.lollito.fm.model.dto.ClubDTO;
import com.lollito.fm.model.dto.LeagueDTO;
import com.lollito.fm.model.dto.SeasonDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSeasonStatsDTO {
    private Long id;
    private SeasonDTO season;
    private ClubDTO club;
    private LeagueDTO league;

    private Integer matchesPlayed;
    private Integer matchesStarted;
    private Integer minutesPlayed;
    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;
    private Integer cleanSheets;
    private Double averageRating;

    // Add other fields as needed
}
