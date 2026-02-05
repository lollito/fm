package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum InjurySeverity {
    MINOR(1, 7),      // 1-7 days
    MODERATE(7, 21),  // 1-3 weeks
    MAJOR(21, 84),    // 3-12 weeks
    SEVERE(84, 365);  // 3-12 months

    private final int minDays;
    private final int maxDays;

    InjurySeverity(int minDays, int maxDays) {
        this.minDays = minDays;
        this.maxDays = maxDays;
    }
}
