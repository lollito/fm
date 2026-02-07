package com.lollito.fm.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.SponsorshipStatus;
import com.lollito.fm.model.SponsorshipType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipDealDTO implements Serializable {
    private Long id;
    private SponsorDTO sponsor;
    private SponsorshipType type;
    private BigDecimal currentAnnualValue;
    private BigDecimal totalValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private SponsorshipStatus status;
}
