package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum SnapshotStatus {
    CREATING("Creating"),
    READY("Ready"),
    CORRUPTED("Corrupted"),
    DELETED("Deleted");

    private final String displayName;

    SnapshotStatus(String displayName) {
        this.displayName = displayName;
    }
}
