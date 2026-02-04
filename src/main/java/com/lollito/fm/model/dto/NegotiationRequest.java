package com.lollito.fm.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationRequest implements Serializable {
    private BigDecimal requestedAnnualValue;
    private Integer requestedContractYears;
}
