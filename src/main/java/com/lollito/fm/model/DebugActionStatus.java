package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum DebugActionStatus {
    PENDING("Pending"),
    EXECUTING("Executing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    DebugActionStatus(String displayName) {
        this.displayName = displayName;
    }
}
