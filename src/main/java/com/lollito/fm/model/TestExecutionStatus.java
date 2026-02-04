package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum TestExecutionStatus {
    RUNNING("Running"),
    PASSED("Passed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    TestExecutionStatus(String displayName) {
        this.displayName = displayName;
    }
}
