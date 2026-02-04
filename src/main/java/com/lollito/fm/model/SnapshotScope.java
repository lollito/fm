package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum SnapshotScope {
    FULL_SYSTEM("Full System"),
    GAME_DATA("Game Data"),
    USER_DATA("User Data"),
    FINANCIAL_DATA("Financial Data"),
    MATCH_DATA("Match Data"),
    CONFIGURATION("Configuration");

    private final String displayName;

    SnapshotScope(String displayName) {
        this.displayName = displayName;
    }
}
