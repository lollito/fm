package com.lollito.fm.model;

public enum ProposalStatus {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn"),
    EXPIRED("Expired");

    private final String displayName;

    ProposalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
