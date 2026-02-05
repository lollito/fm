package com.lollito.fm.dto.request;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class RenewContractRequest implements Serializable {
    private BigDecimal newSalary;
    private Integer contractYears;
    private BigDecimal signingBonus;
    private BigDecimal performanceBonus;
}
