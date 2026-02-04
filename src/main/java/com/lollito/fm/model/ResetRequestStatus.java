package com.lollito.fm.model;

public enum ResetRequestStatus {
    PENDING("Pending"),
    USED("Used"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");

    private final String displayName;

    ResetRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
