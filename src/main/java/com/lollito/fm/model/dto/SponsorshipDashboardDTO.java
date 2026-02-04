package com.lollito.fm.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipDashboardDTO implements Serializable {
    private List<SponsorshipDealDTO> activeDeals;
    private List<SponsorshipOfferDTO> pendingOffers;
    private BigDecimal totalAnnualValue;
    private Integer totalActiveDeals;
    private List<SponsorshipPaymentDTO> recentPayments;
    private ClubAttractivenessDTO clubAttractiveness;
}
