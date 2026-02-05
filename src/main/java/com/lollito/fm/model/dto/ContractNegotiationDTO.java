package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lollito.fm.model.NegotiationStatus;
import com.lollito.fm.model.NegotiationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractNegotiationDTO {
    private Long id;
    private Long playerId;
    private Long clubId;
    private NegotiationType type;
    private NegotiationStatus status;
    private BigDecimal offeredWeeklySalary;
    private BigDecimal offeredSigningBonus;
    private BigDecimal offeredLoyaltyBonus;
    private Integer offeredContractYears;
    private BigDecimal offeredReleaseClause;
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
}
