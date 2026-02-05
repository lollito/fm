package com.lollito.fm.model;

public enum UpgradeType {
    CONSTRUCTION("Construction"),
    CAPACITY_EXPANSION("Capacity Expansion"),
    QUALITY_IMPROVEMENT("Quality Improvement"),
    FEATURE_ADDITION("Feature Addition"),
    TECHNOLOGY_UPGRADE("Technology Upgrade"),
    SAFETY_UPGRADE("Safety Upgrade");

    private final String displayName;

    UpgradeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
