package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ClauseType {
    RELEASE_CLAUSE("Release Clause"),
    LOYALTY_BONUS("Loyalty Bonus"),
    APPEARANCE_BONUS("Appearance Bonus"),
    GOAL_BONUS("Goal Bonus"),
    CLEAN_SHEET_BONUS("Clean Sheet Bonus"),
    PROMOTION_CLAUSE("Promotion Clause"),
    RELEGATION_CLAUSE("Relegation Clause"),
    CHAMPIONS_LEAGUE_CLAUSE("Champions League Qualification Clause"),
    IMAGE_RIGHTS("Image Rights"),
    TERMINATION_CLAUSE("Termination Clause");

    private final String displayName;

    ClauseType(String displayName) {
        this.displayName = displayName;
    }
}
