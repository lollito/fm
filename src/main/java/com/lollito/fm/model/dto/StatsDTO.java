package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {
    private Long id;
    private Integer homeShots;
    private Integer homeOnTarget;
    private Integer homeFouls;
    private Integer homeYellowCards;
    private Integer homePossession;
    private Integer homePasses;
    private Integer homeCompletedPasses;
    private Integer homeTackles;
    private Integer homeInterceptions;

    private Integer awayShots;
    private Integer awayOnTarget;
    private Integer awayFouls;
    private Integer awayYellowCards;
    private Integer awayPossession;
    private Integer awayPasses;
    private Integer awayCompletedPasses;
    private Integer awayTackles;
    private Integer awayInterceptions;
}
