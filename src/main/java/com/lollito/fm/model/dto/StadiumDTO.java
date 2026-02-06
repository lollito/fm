package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StadiumDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer baseCapacity;
    private Integer grandstandNord;
    private Integer grandstandSud;
    private Integer grandstandWest;
    private Integer grandstandEst;
    private Integer pitchQuality;
    private Integer facilitiesQuality;
    private Integer securityLevel;
    private Integer accessibilityLevel;
    private Boolean hasRoof;
    private Boolean hasUndersoilHeating;
    private Boolean hasVipBoxes;
    private Boolean hasMediaCenter;
    private Boolean hasMuseum;
    private Boolean hasMegastore;
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost;
    private BigDecimal upgradeValue;
    private Double atmosphereMultiplier;
    private Double revenueMultiplier;
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
