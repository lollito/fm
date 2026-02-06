package com.lollito.fm.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lollito.fm.model.MatchStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private ClubDTO home;
    private ClubDTO away;
    private Integer homeScore;
    private Integer awayScore;
    @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime date;
    private Boolean finish;
    private MatchStatus status;
    private Boolean last;
    private Integer spectators;
    private List<EventHistoryDTO> events;
    private StatsDTO stats;

    private FormationDTO homeFormation;
    private FormationDTO awayFormation;
    private List<MatchPlayerStatsDTO> playerStats;

    // Transient fields from Match entity logic
    private Integer roundNumber;
    private String competitionName;
    private String stadiumName;
}
