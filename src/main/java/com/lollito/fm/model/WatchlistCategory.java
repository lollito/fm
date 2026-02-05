package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum WatchlistCategory {
    TARGET("Primary Target"),
    BACKUP("Backup Option"),
    FUTURE("Future Prospect"),
    COMPARISON("Comparison Player"),
    LOAN_TARGET("Loan Target");

    private final String displayName;

    WatchlistCategory(String displayName) {
        this.displayName = displayName;
    }
}
