package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum SnapshotType {
    MANUAL("Manual"),
    AUTOMATIC("Automatic"),
    PRE_DEBUG("Pre-Debug"),
    SCHEDULED("Scheduled");

    private final String displayName;

    SnapshotType(String displayName) {
        this.displayName = displayName;
    }
}
