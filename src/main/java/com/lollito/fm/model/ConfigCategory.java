package com.lollito.fm.model;

public enum ConfigCategory {
    GAME_SETTINGS("Game Settings"),
    MATCH_SIMULATION("Match Simulation"),
    PLAYER_GENERATION("Player Generation"),
    FINANCIAL("Financial"),
    SECURITY("Security"),
    PERFORMANCE("Performance");

    private final String displayName;

    ConfigCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
