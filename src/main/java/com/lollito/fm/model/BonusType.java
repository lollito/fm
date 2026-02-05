package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum BonusType {
    LEAGUE_POSITION("League Position"),
    CUP_PROGRESS("Cup Progress"),
    ATTENDANCE("Attendance"),
    REPUTATION("Reputation"),
    GOALS_SCORED("Goals Scored"),
    CLEAN_SHEETS("Clean Sheets");

    private final String displayName;

    BonusType(String displayName) {
        this.displayName = displayName;
    }
}
