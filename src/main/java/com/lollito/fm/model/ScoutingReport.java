package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
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
@Table(name = "scouting_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ScoutingReport implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private ScoutingAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scout_id")
    private Scout scout;

    private LocalDate reportDate;

    // Revealed player attributes (with accuracy based on scout ability)
    private Double revealedStamina;
    private Double revealedPlaymaking;
    private Double revealedScoring;
    private Double revealedWinger;
    private Double revealedGoalkeeping;
    private Double revealedPassing;
    private Double revealedDefending;
    private Double revealedSetPieces;

    // Scout's assessment
    private Integer overallRating; // 1-100
    private Integer potentialRating; // 1-100
    private Double accuracyLevel; // 0.0-1.0, how accurate the report is

    @Enumerated(EnumType.STRING)
    private RecommendationLevel recommendation; // AVOID, MONITOR, CONSIDER, RECOMMEND, PRIORITY

    @Column(length = 4000)
    private String strengths; // Text description of player strengths

    @Column(length = 4000)
    private String weaknesses; // Text description of player weaknesses

    @Column(length = 4000)
    private String personalityAssessment;

    @Column(length = 4000)
    private String injuryHistory;

    // Market information
    private BigDecimal estimatedValue;
    private BigDecimal estimatedWage;
    private Boolean isAvailableForTransfer;
    private LocalDate contractExpiry;

    @Column(length = 4000)
    private String additionalNotes;
    private Integer confidenceLevel; // 1-10, scout's confidence in the report
}
