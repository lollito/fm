package com.lollito.fm.model;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public enum SponsorTier {
    PREMIUM("Premium Sponsor", BigDecimal.valueOf(5000000), BigDecimal.valueOf(1000000)),
    STANDARD("Standard Sponsor", BigDecimal.valueOf(2000000), BigDecimal.valueOf(250000)),
    BUDGET("Budget Sponsor", BigDecimal.valueOf(500000), BigDecimal.valueOf(50000));

    private final String displayName;
    private final BigDecimal maxBudget;
    private final BigDecimal minBudget;

    SponsorTier(String displayName, BigDecimal maxBudget, BigDecimal minBudget) {
        this.displayName = displayName;
        this.maxBudget = maxBudget;
        this.minBudget = minBudget;
    }
}
