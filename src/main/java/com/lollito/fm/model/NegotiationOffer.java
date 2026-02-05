package com.lollito.fm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "negotiation_offer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id")
    @JsonIgnore
    private ContractNegotiation negotiation;

    @Enumerated(EnumType.STRING)
    private OfferSide offerSide; // CLUB, PLAYER

    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;
    private Integer contractYears;
    private BigDecimal releaseClause;

    private LocalDateTime offerDate;

    @Enumerated(EnumType.STRING)
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, COUNTERED

    private String notes;
}
