package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lollito.fm.model.OfferSide;
import com.lollito.fm.model.OfferStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationOfferDTO {
    private Long id;
    private Long negotiationId;
    private OfferSide offerSide;
    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;
    private Integer contractYears;
    private BigDecimal releaseClause;
    private LocalDateTime offerDate;
    private OfferStatus status;
    private String notes;
}
