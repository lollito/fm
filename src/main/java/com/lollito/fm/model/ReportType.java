package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum ReportType {
    MONTHLY("Monthly Report"),
    QUARTERLY("Quarterly Report"),
    ANNUAL("Annual Report"),
    SEASON("Season Report");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }
}
