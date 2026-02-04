package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum DebugActionType {
    ADVANCE_SEASON("Advance Season"),
    SIMULATE_MATCHES("Simulate Matches"),
    MODIFY_PLAYER_STATS("Modify Player Stats"),
    ADJUST_FINANCES("Adjust Finances"),
    FORCE_TRANSFERS("Force Transfers"),
    RESET_LEAGUE("Reset League"),
    GENERATE_DATA("Generate Data"),
    SYSTEM_MAINTENANCE("System Maintenance"),
    DATABASE_OPERATION("Database Operation");

    private final String displayName;

    DebugActionType(String displayName) {
        this.displayName = displayName;
    }
}
