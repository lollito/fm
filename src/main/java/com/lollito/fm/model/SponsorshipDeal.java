package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

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
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "sponsorship_deal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SponsorshipDeal implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private Club club;

    private String sponsorName;
    private String sponsorLogo;

    @Enumerated(EnumType.STRING)
    private SponsorshipType type; // SHIRT, STADIUM, TRAINING_GROUND, GENERAL

    private BigDecimal annualValue;
    private BigDecimal totalValue;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status; // ACTIVE, EXPIRED, TERMINATED

    // Performance bonuses
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;

    private String contractTerms;
    private Boolean autoRenewal;
    private Integer renewalYears;
}
