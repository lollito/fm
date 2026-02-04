package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ScoutingLevel {
    UNKNOWN("Unknown", 0.0),
    BASIC("Basic Knowledge", 0.3),
    DETAILED("Detailed Knowledge", 0.7),
    COMPREHENSIVE("Comprehensive Knowledge", 1.0);

    private final String displayName;
    private final double accuracyMultiplier;

    ScoutingLevel(String displayName, double accuracyMultiplier) {
        this.displayName = displayName;
        this.accuracyMultiplier = accuracyMultiplier;
    }
}
