package com.lollito.fm.model;

public enum LoanRecommendation {
    CONTINUE("Continue Loan"),
    RECALL("Recall Player"),
    EXTEND("Extend Loan"),
    PURCHASE("Activate Purchase Option");

    private final String displayName;

    LoanRecommendation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
