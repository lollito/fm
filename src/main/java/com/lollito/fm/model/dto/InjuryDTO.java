package com.lollito.fm.model.dto;

import java.time.LocalDate;

import com.lollito.fm.model.InjurySeverity;
import com.lollito.fm.model.InjuryStatus;
import com.lollito.fm.model.InjuryType;

import lombok.Data;

@Data
public class InjuryDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private InjuryType type;
    private InjurySeverity severity;
    private LocalDate injuryDate;
    private LocalDate expectedRecoveryDate;
    private LocalDate actualRecoveryDate;
    private InjuryStatus status;
    private Double performanceImpact;
    private String description;
}
