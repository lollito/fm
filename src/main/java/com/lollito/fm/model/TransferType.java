package com.lollito.fm.model;

public enum TransferType {
    PURCHASE("Purchase"),
    LOAN("Loan"),
    FREE_TRANSFER("Free Transfer"),
    YOUTH_PROMOTION("Youth Promotion"),
    RETIREMENT("Retirement");

    private final String displayName;

    TransferType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
