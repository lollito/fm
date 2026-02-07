package com.lollito.fm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.RecommendationLevel;

import lombok.Data;

@Data
public class ScoutingReportDTO {
    private Long id;
    private Long assignmentId;
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private ScoutDTO scout;
    private LocalDate reportDate;
    private Double revealedStamina;
    private Double revealedPlaymaking;
    private Double revealedScoring;
    private Double revealedWinger;
    private Double revealedGoalkeeping;
    private Double revealedPassing;
    private Double revealedDefending;
    private Double revealedSetPieces;
    private Integer overallRating;
    private Integer potentialRating;
    private Double accuracyLevel;
    private RecommendationLevel recommendation;
    private String strengths;
    private String weaknesses;
    private java.util.List<String> strengthsList;
    private java.util.List<String> weaknessesList;
    private String personalityAssessment;
    private String injuryHistory;
    private BigDecimal estimatedValue;
    private BigDecimal estimatedWage;
    private Boolean isAvailableForTransfer;
    private LocalDate contractExpiry;
    private String additionalNotes;
    private Integer confidenceLevel;
}
