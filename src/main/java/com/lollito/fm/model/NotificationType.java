package com.lollito.fm.model;

public enum NotificationType {
    SYSTEM("System Notification"),
    MATCH_RESULT("Match Result"),
    TRANSFER_UPDATE("Transfer Update"),
    FINANCIAL_ALERT("Financial Alert"),
    ACCOUNT_SECURITY("Account Security"),
    PROMOTIONAL("Promotional");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
