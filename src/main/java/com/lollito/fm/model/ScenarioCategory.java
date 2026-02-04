package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ScenarioCategory {
    MATCH_SIMULATION("Match Simulation"),
    PLAYER_DEVELOPMENT("Player Development"),
    FINANCIAL_SYSTEM("Financial System"),
    TRANSFER_SYSTEM("Transfer System"),
    LEAGUE_PROGRESSION("League Progression"),
    USER_INTERACTION("User Interaction"),
    PERFORMANCE("Performance");

    private final String displayName;

    ScenarioCategory(String displayName) {
        this.displayName = displayName;
    }
}
