package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id")
    @ToString.Exclude
    private Sponsor sponsor;

    @Enumerated(EnumType.STRING)
    private SponsorshipType type;

    private BigDecimal baseAnnualValue;
    private BigDecimal currentAnnualValue; // Adjusted based on performance
    private BigDecimal totalValue;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer contractYears;

    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status;

    // Performance bonuses
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;
    private BigDecimal reputationBonus;

    // Contract terms
    private String contractTerms;
    private Boolean autoRenewal;
    private Integer renewalYears;
    private BigDecimal renewalBonusPercentage;

    // Performance tracking
    private Integer currentLeaguePosition;
    private Integer bestLeaguePosition;
    private Integer cupRoundsReached;
    private Double averageAttendance;
    private Integer reputationScore;

    @OneToMany(mappedBy = "sponsorshipDeal", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<SponsorshipPayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "sponsorshipDeal", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<SponsorshipBonus> bonuses = new ArrayList<>();
}
