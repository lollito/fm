package com.lollito.fm.model.rest;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSeasonStatsDTO {
    private Long id;
    private Season season; // Or SeasonDTO
    private Club club;     // Or ClubDTO
    private League league; // Or LeagueDTO

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
