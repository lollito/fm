package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum RecommendationLevel {
    AVOID("Avoid", "#f44336"),
    MONITOR("Monitor", "#ff9800"),
    CONSIDER("Consider", "#2196f3"),
    RECOMMEND("Recommend", "#4caf50"),
    PRIORITY("Priority Target", "#9c27b0");

    private final String displayName;
    private final String color;

    RecommendationLevel(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
}
