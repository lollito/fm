package com.lollito.fm.model;

public enum NotificationType {
    SYSTEM("System Notification"),
    MATCH_RESULT("Match Result"),
    TRANSFER_UPDATE("Transfer Update"),
    FINANCIAL_ALERT("Financial Alert"),
    ACCOUNT_SECURITY("Account Security"),
    PROMOTIONAL("Promotional"),
    QUEST_COMPLETED("Quest Completed"),
    LEVEL_UP("Level Up"),
    ACHIEVEMENT_UNLOCKED("Achievement Unlocked");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
