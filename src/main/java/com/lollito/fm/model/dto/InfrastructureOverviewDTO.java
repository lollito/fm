package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.util.List;

import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.TrainingFacility;
import com.lollito.fm.model.MedicalCenter;
import com.lollito.fm.model.YouthAcademy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InfrastructureOverviewDTO {
    private Stadium stadium;
    private TrainingFacility trainingFacility;
    private MedicalCenter medicalCenter;
    private YouthAcademy youthAcademy;
    private List<FacilityUpgradeDTO> ongoingUpgrades;
    private List<MaintenanceRecordDTO> upcomingMaintenance;
    private BigDecimal totalMonthlyMaintenanceCost;
    private BigDecimal totalInfrastructureValue;
}
