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
public class TrainingFacilityDTO implements Serializable {
    private Long id;
    private String name;
    private Integer overallQuality;
    private Integer pitchQuality;
    private Integer gymQuality;
    private Integer medicalFacilityQuality;
    private Integer analysisRoomQuality;
    private Double physicalTrainingBonus;
    private Double technicalTrainingBonus;
    private Double tacticalTrainingBonus;
    private Double mentalTrainingBonus;
    private Integer numberOfPitches;
    private Boolean hasIndoorFacilities;
    private Boolean hasHydrotherapy;
    private Boolean hasAltitudeTraining;
    private Boolean hasVideoAnalysis;
    private Boolean hasNutritionCenter;
    private BigDecimal constructionCost;
    private BigDecimal maintenanceCost;
    private BigDecimal upgradeValue;
    private LocalDate lastUpgradeDate;
    private LocalDate nextMaintenanceDate;
}
