package com.lollito.fm.dto;

import java.math.BigDecimal;

import com.lollito.fm.model.RecommendationLevel;

import lombok.Data;

@Data
public class ScoutingRecommendationDTO {
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private String playerRole;
    private Integer playerAge;
    private String currentClubName;
    private Integer overallRating;
    private Integer potentialRating;
    private RecommendationLevel recommendation;
    private BigDecimal estimatedValue;
}
