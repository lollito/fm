package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum OfferStatus {
    PENDING("Pending Review"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    NEGOTIATING("Under Negotiation");

    private final String displayName;

    OfferStatus(String displayName) {
        this.displayName = displayName;
    }
}
