package com.lollito.fm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.ContractStatus;

import lombok.Data;

@Data
public class StaffContractDTO implements Serializable {
    private Long id;
    private Long staffId;
    private Long clubId;
    private BigDecimal monthlySalary;
    private BigDecimal signingBonus;
    private BigDecimal performanceBonus;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
    private String terminationClause;
    private BigDecimal terminationFee;
}
