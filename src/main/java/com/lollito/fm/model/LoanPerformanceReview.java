package com.lollito.fm.model;

import java.io.Serializable;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "loan_performance_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class LoanPerformanceReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_agreement_id")
    @ToString.Exclude
    private LoanAgreement loanAgreement;

    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    private ReviewPeriod period; // MONTHLY, QUARTERLY, MID_SEASON, END_SEASON

    // Performance metrics
    private Integer matchesPlayed;
    private Integer goals;
    private Integer assists;
    private Double averageRating;
    private Integer yellowCards;
    private Integer redCards;

    // Development assessment
    private Double skillImprovement; // Overall skill improvement
    private String developmentNotes;
    private Boolean targetsMet;

    // Recommendations
    @Enumerated(EnumType.STRING)
    private LoanRecommendation recommendation; // CONTINUE, RECALL, EXTEND, PURCHASE

    private String reviewNotes;
    private String parentClubFeedback;
    private String loanClubFeedback;
}
