package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.FacilityType;
import com.lollito.fm.model.MaintenanceType;

import lombok.Data;

@Data
public class ScheduleMaintenanceRequest {
    private FacilityType facilityType;
    private Long facilityId;
    private MaintenanceType maintenanceType;
    private String description;
    private BigDecimal cost;
    private LocalDate scheduledDate;
    private String contractorName;
}
