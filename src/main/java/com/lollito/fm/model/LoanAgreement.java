package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "loan_agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class LoanAgreement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @ToString.Exclude
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_club_id")
    @ToString.Exclude
    private Club parentClub; // Club that owns the player

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_club_id")
    @ToString.Exclude
    private Club loanClub; // Club receiving the player on loan

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status; // PROPOSED, ACTIVE, COMPLETED, TERMINATED, RECALLED

    // Financial terms
    private BigDecimal loanFee; // One-time fee paid by loan club
    private Double parentClubSalaryShare; // 0.0 to 1.0 (percentage parent club pays)
    private Double loanClubSalaryShare; // 0.0 to 1.0 (percentage loan club pays)

    // Loan conditions
    private Boolean hasRecallClause; // Can parent club recall player?
    private LocalDate earliestRecallDate;
    private Boolean hasOptionToBuy; // Option to buy at end of loan
    private BigDecimal optionToBuyPrice;
    private Boolean hasObligationToBuy; // Mandatory purchase if conditions met
    private String obligationConditions; // JSON conditions for mandatory purchase

    // Performance tracking
    private Integer minimumAppearances; // Minimum games player must play
    private Integer actualAppearances;
    private Boolean developmentTargetsMet;
    private String developmentTargets; // JSON development goals

    // Agreement details
    private String loanReason; // Why player is being loaned
    private String specialConditions; // Additional terms
    private LocalDateTime agreementDate;

    @OneToMany(mappedBy = "loanAgreement", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<LoanPerformanceReview> performanceReviews = new ArrayList<>();
}
