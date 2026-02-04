package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.SponsorshipStatus;
import com.lollito.fm.model.SponsorshipType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SponsorshipDealDTO {
    private Long id;
    private String sponsorName;
    private String sponsorLogo;
    private SponsorshipType type;
    private BigDecimal annualValue;
    private BigDecimal totalValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private SponsorshipStatus status;
}
