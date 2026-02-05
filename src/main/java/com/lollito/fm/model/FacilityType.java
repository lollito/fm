package com.lollito.fm.model;

public enum FacilityType {
    STADIUM("Stadium"),
    TRAINING_FACILITY("Training Facility"),
    MEDICAL_CENTER("Medical Center"),
    YOUTH_ACADEMY("Youth Academy");

    private final String displayName;

    FacilityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
