package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.GenericGenerator;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "sponsorship_offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SponsorshipOffer implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id")
    @ToString.Exclude
    private Sponsor sponsor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private Club club;

    @Enumerated(EnumType.STRING)
    private SponsorshipType type;

    private BigDecimal offeredAnnualValue;
    private Integer contractYears;

    // Performance bonuses offered
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;

    @Enumerated(EnumType.STRING)
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, EXPIRED

    private LocalDate offerDate;
    private LocalDate expiryDate;

    private String terms;
    private String specialConditions;

    // Negotiation tracking
    private Integer negotiationRounds;
    private BigDecimal lastCounterOffer;
    private String rejectionReason;
}
