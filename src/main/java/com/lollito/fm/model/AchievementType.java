package com.lollito.fm.model;

public enum AchievementType {
    MILESTONE("Milestone Achievement"),
    PERFORMANCE("Performance Achievement"),
    TEAM_SUCCESS("Team Success"),
    INDIVIDUAL_AWARD("Individual Award"),
    RECORD("Record Achievement");

    private final String displayName;

    AchievementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
