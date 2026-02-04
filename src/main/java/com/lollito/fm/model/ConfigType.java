package com.lollito.fm.model;

public enum ConfigType {
    STRING("String"),
    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    DECIMAL("Decimal"),
    JSON("JSON");

    private final String displayName;

    ConfigType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
