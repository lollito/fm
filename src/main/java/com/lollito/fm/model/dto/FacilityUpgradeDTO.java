package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.UpgradeStatus;
import com.lollito.fm.model.UpgradeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacilityUpgradeDTO {
    private Long id;
    private FacilityType facilityType;
    private Long facilityId;
    private UpgradeType upgradeType;
    private String upgradeName;
    private String description;
    private BigDecimal cost;
    private Integer durationDays;
    private Integer qualityImprovement;
    private String bonusEffects;
    private Double maintenanceCostIncrease;
    private UpgradeStatus status;
    private LocalDate startDate;
    private LocalDate completionDate;
    private LocalDate plannedCompletionDate;
    private Integer requiredQualityLevel;
    private String requiredUpgrades;
    private String contractorName;
    private String notes;
}
