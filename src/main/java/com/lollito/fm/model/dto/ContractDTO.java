package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.lollito.fm.model.ContractStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {
    private Long id;
    private Long playerId;
    private Long clubId;
    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal releaseClause;
    private Boolean hasReleaseClause;
    private ContractStatus status;
    private Integer negotiationAttempts;
}
