package com.lollito.fm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.StaffRole;
import com.lollito.fm.model.StaffSpecialization;
import com.lollito.fm.model.StaffStatus;

import lombok.Data;

@Data
public class StaffDTO implements Serializable {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birth;
    private Integer age;
    private Long clubId;
    private StaffRole role;
    private StaffSpecialization specialization;
    private Integer ability;
    private Integer reputation;
    private BigDecimal monthlySalary;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private StaffStatus status;
    private Double motivationBonus;
    private Double trainingBonus;
    private Double injuryPreventionBonus;
    private Double recoveryBonus;
    private Double scoutingBonus;
    private String nationalityName;
    private String description;
    private Integer experience;
}
