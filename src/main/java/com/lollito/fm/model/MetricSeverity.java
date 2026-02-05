package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum MetricSeverity {
    NORMAL("Normal"),
    WARNING("Warning"),
    CRITICAL("Critical");

    private final String displayName;

    MetricSeverity(String displayName) {
        this.displayName = displayName;
    }
}
