package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum SponsorshipType {
    SHIRT("Shirt Sponsor"),
    STADIUM("Stadium Naming Rights"),
    TRAINING_GROUND("Training Ground"),
    GENERAL("General Sponsor");

    private final String displayName;

    SponsorshipType(String displayName) {
        this.displayName = displayName;
    }
}
