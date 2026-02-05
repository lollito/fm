package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum WatchlistNotificationType {
    PERFORMANCE("Performance Update"),
    TRANSFER_STATUS("Transfer Status Change"),
    INJURY("Injury Update"),
    CONTRACT_EXPIRY("Contract Expiring"),
    PRICE_CHANGE("Price Change"),
    MATCH_PERFORMANCE("Match Performance"),
    AVAILABILITY("Availability Change"),
    COMPETITION("Competition Interest");

    private final String displayName;

    WatchlistNotificationType(String displayName) {
        this.displayName = displayName;
    }
}
