package com.lollito.fm.model;

public enum ActivityType {
    LOGIN("Login"),
    LOGOUT("Logout"),
    PASSWORD_CHANGE("Password Change"),
    PROFILE_UPDATE("Profile Update"),
    CLUB_ACTION("Club Action"),
    PLAYER_ACTION("Player Action"),
    MATCH_ACTION("Match Action"),
    TRANSFER_ACTION("Transfer Action"),
    FINANCIAL_ACTION("Financial Action"),
    SYSTEM_ACTION("System Action");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
