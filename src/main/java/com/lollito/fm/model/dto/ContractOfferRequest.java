package com.lollito.fm.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractOfferRequest {
    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;
    private Integer contractYears;
    private BigDecimal releaseClause;
}
