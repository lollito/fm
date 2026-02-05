package com.lollito.fm.model.dto;

import java.math.BigDecimal;

import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.UpgradeType;

import lombok.Data;

@Data
public class StartUpgradeRequest {
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
    private String contractorName;
}
