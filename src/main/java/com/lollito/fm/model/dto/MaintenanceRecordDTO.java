package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.MaintenanceStatus;
import com.lollito.fm.model.MaintenanceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceRecordDTO {
    private Long id;
    private FacilityType facilityType;
    private Long facilityId;
    private MaintenanceType maintenanceType;
    private String description;
    private BigDecimal cost;
    private MaintenanceStatus status;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private Integer qualityRestored;
    private String issuesFound;
    private String workPerformed;
    private String contractorName;
    private Boolean isEmergencyMaintenance;
}
