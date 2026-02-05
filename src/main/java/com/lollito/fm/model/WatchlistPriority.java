package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum WatchlistPriority {
    LOW("Low Priority", "#4caf50"),
    MEDIUM("Medium Priority", "#ff9800"),
    HIGH("High Priority", "#f44336"),
    URGENT("Urgent", "#9c27b0");

    private final String displayName;
    private final String color;

    WatchlistPriority(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
}
