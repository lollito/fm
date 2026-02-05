package com.lollito.fm.model.dto;

import com.lollito.fm.model.OfferSide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounterOfferRequest {
    private OfferSide offerSide;
    private ContractOfferRequest offer;
}
