package com.lollito.fm.model;

public enum ActionStatus {
    SUCCESS("Success"),
    FAILED("Failed"),
    PENDING("Pending");

    private final String displayName;

    ActionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
