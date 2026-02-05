package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
