package com.lollito.fm.dto;

import java.time.LocalDate;

import com.lollito.fm.model.ScoutingLevel;

import lombok.Data;

@Data
public class PlayerScoutingStatusDTO {
    private Long id;
    private Long playerId;
    private Long clubId;
    private ScoutingLevel scoutingLevel;
    private LocalDate lastScoutedDate;
    private LocalDate firstScoutedDate;
    private Integer timesScoutedThisSeason;
    private Double knowledgeAccuracy;
    private Double knownStamina;
    private Double knownPlaymaking;
    private Double knownScoring;
    private Double knownWinger;
    private Double knownGoalkeeping;
    private Double knownPassing;
    private Double knownDefending;
    private Double knownSetPieces;
}
