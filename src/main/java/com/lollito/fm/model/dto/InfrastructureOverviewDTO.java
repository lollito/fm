package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InfrastructureOverviewDTO {
    private StadiumDTO stadium;
    private TrainingFacilityDTO trainingFacility;
    private MedicalCenterDTO medicalCenter;
    private YouthAcademyDTO youthAcademy;
    private List<FacilityUpgradeDTO> ongoingUpgrades;
    private List<MaintenanceRecordDTO> upcomingMaintenance;
    private BigDecimal totalMonthlyMaintenanceCost;
    private BigDecimal totalInfrastructureValue;
}
