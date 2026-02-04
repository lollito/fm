package com.lollito.fm.model;

public enum AdminActionType {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    ACTIVATE("Activate"),
    DEACTIVATE("Deactivate"),
    RESET("Reset"),
    BULK_UPDATE("Bulk Update"),
    IMPORT("Import"),
    EXPORT("Export");

    private final String displayName;

    AdminActionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
