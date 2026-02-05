package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {
    public enum OfferDecision {
        ACCEPT, REJECT, COUNTER
    }

    private OfferDecision decision;
    private String reason;
}
