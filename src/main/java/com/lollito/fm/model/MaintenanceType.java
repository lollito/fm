package com.lollito.fm.model;

public enum MaintenanceType {
    ROUTINE("Routine Maintenance"),
    PREVENTIVE("Preventive Maintenance"),
    CORRECTIVE("Corrective Maintenance"),
    EMERGENCY("Emergency Maintenance"),
    SEASONAL("Seasonal Maintenance");

    private final String displayName;

    MaintenanceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
