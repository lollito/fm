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
public class AdjustFinancesRequest {
    private Long clubId;
    private BigDecimal balanceAdjustment;
    private BigDecimal setBalance;
    private BigDecimal debtAdjustment;
}
