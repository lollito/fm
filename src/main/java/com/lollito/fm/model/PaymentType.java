package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum PaymentType {
    BASE_PAYMENT("Base Payment"),
    PERFORMANCE_BONUS("Performance Bonus"),
    RENEWAL_BONUS("Renewal Bonus"),
    SIGNING_BONUS("Signing Bonus");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }
}
