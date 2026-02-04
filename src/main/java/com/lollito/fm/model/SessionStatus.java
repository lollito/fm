package com.lollito.fm.model;

public enum SessionStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    TERMINATED("Terminated");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
