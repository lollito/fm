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
public class MedicalCenterDTO implements Serializable {
    private Long id;
    private String name;
    private Integer overallQuality;
    private Integer diagnosticEquipment;
    private Integer rehabilitationFacilities;
    private Integer surgicalCapabilities;
    private Integer preventiveCareLevel;
    private Double injuryPreventionBonus;
    private Double recoverySpeedBonus;
    private Double fitnessMaintenanceBonus;
    private Boolean hasMriScanner;
    private Boolean hasHyperbaricChamber;
    private Boolean hasCryotherapy;
    private Boolean hasPhysiotherapy;
    private Boolean hasSportsScience;
    private Integer numberOfDoctors;
    private Integer numberOfPhysiotherapists;
    private Integer numberOfSportsScientists;
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost;
    private BigDecimal upgradeValue;
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
