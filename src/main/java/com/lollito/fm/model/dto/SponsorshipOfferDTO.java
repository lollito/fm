package com.lollito.fm.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.OfferStatus;
import com.lollito.fm.model.SponsorshipType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipOfferDTO implements Serializable {
    private Long id;
    private SponsorDTO sponsor;
    private SponsorshipType type;
    private BigDecimal offeredAnnualValue;
    private Integer contractYears;
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;
    private OfferStatus status;
    private LocalDate offerDate;
    private LocalDate expiryDate;
    private String terms;
}
