package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ScoutSpecialization {
    GOALKEEPERS("Goalkeepers"),
    DEFENDERS("Defenders"),
    MIDFIELDERS("Midfielders"),
    FORWARDS("Forwards"),
    YOUTH_PLAYERS("Youth Players"),
    OPPOSITION_ANALYSIS("Opposition Analysis"),
    GENERAL("General Scouting");

    private final String displayName;

    ScoutSpecialization(String displayName) {
        this.displayName = displayName;
    }
}
