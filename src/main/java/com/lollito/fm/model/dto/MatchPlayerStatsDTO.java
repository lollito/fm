package com.lollito.fm.model.dto;

import com.lollito.fm.dto.PlayerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayerStatsDTO {
    private Long id;
    private PlayerDTO player;
    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;
    private Integer shots;
    private Integer shotsOnTarget;
    private Integer passes;
    private Integer completedPasses;
    private Integer tackles;
    private Double rating;
    private Boolean mvp;
    private String position;
}
