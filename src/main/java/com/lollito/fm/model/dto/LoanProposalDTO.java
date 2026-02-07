package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lollito.fm.model.ProposalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanProposalDTO {
    private Long id;

    private Long playerId;
    private String playerName;

    private Long proposingClubId;
    private String proposingClubName;

    private Long targetClubId;
    private String targetClubName;

    private ProposalStatus status;

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
