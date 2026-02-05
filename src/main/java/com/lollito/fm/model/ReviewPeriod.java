package com.lollito.fm.model;

public enum ReviewPeriod {
    MONTHLY("Monthly Review"),
    QUARTERLY("Quarterly Review"),
    MID_SEASON("Mid-Season Review"),
    END_SEASON("End of Season Review");

    private final String displayName;

    ReviewPeriod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
