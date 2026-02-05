package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "loan_proposal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class LoanProposal implements Serializable {

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
    @JoinColumn(name = "proposing_club_id")
    @ToString.Exclude
    private Club proposingClub; // Club making the loan request

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_club_id")
    @ToString.Exclude
    private Club targetClub; // Club that owns the player

    @Enumerated(EnumType.STRING)
    private ProposalStatus status; // PENDING, ACCEPTED, REJECTED, WITHDRAWN, EXPIRED

    // Proposed terms
    private LocalDate proposedStartDate;
    private LocalDate proposedEndDate;
    private BigDecimal proposedLoanFee;
    private Double proposedSalaryShare;
    private Boolean proposedRecallClause;
    private Boolean proposedOptionToBuy;
    private BigDecimal proposedOptionPrice;

    private String proposalMessage;
    private String rejectionReason;

    private LocalDateTime proposalDate;
    private LocalDateTime responseDate;
    private LocalDateTime expiryDate;
}
