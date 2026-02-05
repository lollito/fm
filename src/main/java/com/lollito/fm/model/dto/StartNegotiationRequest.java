package com.lollito.fm.model.dto;

import com.lollito.fm.model.NegotiationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartNegotiationRequest {
    private Long playerId;
    private Long clubId;
    private NegotiationType type;
    private ContractOfferRequest initialOffer;
}
