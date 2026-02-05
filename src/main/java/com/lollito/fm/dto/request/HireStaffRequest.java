package com.lollito.fm.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class HireStaffRequest implements Serializable {
    private Long clubId;
    private Long staffId;
    private Integer contractYears;
    private BigDecimal signingBonus;
    private BigDecimal performanceBonus;
}
