package com.lollito.fm.model.dto;

import java.io.Serializable;
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
public class YouthAcademyDTO implements Serializable {
    private Long id;
    private String name;
    private Integer overallQuality;
    private Integer coachingQuality;
    private Integer facilitiesQuality;
    private Integer educationQuality;
    private Integer scoutingNetwork;
    private Double talentGenerationBonus;
    private Double developmentSpeedBonus;
    private Double retentionBonus;
    private Integer numberOfPitches;
    private Boolean hasEducationCenter;
    private Boolean hasResidentialFacilities;
    private Boolean hasScoutingNetwork;
    private Boolean hasPartnershipPrograms;
    private Integer maxYouthPlayers;
    private Integer numberOfCoaches;
    private Integer numberOfScouts;
    private Integer numberOfEducators;
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost;
    private BigDecimal upgradeValue;
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
