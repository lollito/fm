package com.lollito.fm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.ScoutSpecialization;
import com.lollito.fm.model.ScoutStatus;

import lombok.Data;

@Data
public class ScoutDTO {
    private Long id;
    private String name;
    private String surname;
    private Long clubId;
    private Long regionId;
    private String regionName;
    private Integer ability;
    private Integer reputation;
    private BigDecimal monthlySalary;
    private ScoutSpecialization specialization;
    private ScoutStatus status;
    private LocalDate contractEnd;
    private Integer experience;
}
