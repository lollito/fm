package com.lollito.fm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contract_negotiation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractNegotiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonIgnore
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_contract_id")
    private Contract currentContract;

    @Enumerated(EnumType.STRING)
    private NegotiationType type; // NEW_CONTRACT, RENEWAL, RENEGOTIATION

    @Enumerated(EnumType.STRING)
    private NegotiationStatus status; // PENDING, IN_PROGRESS, ACCEPTED, REJECTED, EXPIRED

    // Club's offer
    private BigDecimal offeredWeeklySalary;
    private BigDecimal offeredSigningBonus;
    private BigDecimal offeredLoyaltyBonus;
    private Integer offeredContractYears;
    private BigDecimal offeredReleaseClause;

    // Player's demands
    private BigDecimal demandedWeeklySalary;
    private BigDecimal demandedSigningBonus;
    private BigDecimal demandedLoyaltyBonus;
    private Integer demandedContractYears;
    private BigDecimal demandedReleaseClause;

    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private LocalDateTime lastOfferDate;

    private Integer roundsOfNegotiation;
    private String rejectionReason;

    @OneToMany(mappedBy = "negotiation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NegotiationOffer> offers = new ArrayList<>();
}
