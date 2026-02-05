package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum StaffSpecialization {
    // Coaching specializations
    ATTACKING_PLAY("Attacking Play"),
    DEFENSIVE_PLAY("Defensive Play"),
    SET_PIECES("Set Pieces"),
    YOUTH_DEVELOPMENT("Youth Development"),
    PLAYER_DEVELOPMENT("Player Development"),

    // Medical specializations
    INJURY_PREVENTION("Injury Prevention"),
    REHABILITATION("Rehabilitation"),
    SPORTS_PSYCHOLOGY("Sports Psychology"),

    // Scouting specializations
    DOMESTIC_SCOUTING("Domestic Scouting"),
    INTERNATIONAL_SCOUTING("International Scouting"),
    YOUTH_SCOUTING("Youth Scouting"),
    OPPOSITION_ANALYSIS("Opposition Analysis");

    private final String displayName;

    StaffSpecialization(String displayName) {
        this.displayName = displayName;
    }
}
