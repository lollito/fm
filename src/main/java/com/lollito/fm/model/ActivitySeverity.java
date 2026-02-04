package com.lollito.fm.model;

public enum ActivitySeverity {
    INFO("Information"),
    WARNING("Warning"),
    ERROR("Error");

    private final String displayName;

    ActivitySeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
