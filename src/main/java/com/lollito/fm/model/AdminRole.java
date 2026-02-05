package com.lollito.fm.model;

public enum AdminRole {
    SUPER_ADMIN("Super Administrator"),
    LEAGUE_ADMIN("League Administrator"),
    MODERATOR("Moderator");

    private final String displayName;

    AdminRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
