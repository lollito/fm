package com.lollito.fm.model.dto;

import java.math.BigDecimal;

import com.lollito.fm.model.BonusType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPerformanceBonusRequest {
    private BonusType type;
    private Integer targetValue;
    private BigDecimal bonusAmount;
    private String description;
}
