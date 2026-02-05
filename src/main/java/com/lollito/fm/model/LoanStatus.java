package com.lollito.fm.model;

public enum LoanStatus {
    PROPOSED("Proposed"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    TERMINATED("Terminated Early"),
    RECALLED("Recalled by Parent Club");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
