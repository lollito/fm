package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ScoutingType {
    PLAYER("Individual Player"),
    CLUB("Club Overview"),
    REGION("Regional Scouting"),
    OPPOSITION("Opposition Analysis");

    private final String displayName;

    ScoutingType(String displayName) {
        this.displayName = displayName;
    }
}
