package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lollito.fm.model.LoanStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAgreementDTO {
    private Long id;

    private Long playerId;
    private String playerName;
    private String playerSurname;

    private Long parentClubId;
    private String parentClubName;

    private Long loanClubId;
    private String loanClubName;

    private LocalDate startDate;
    private LocalDate endDate;

    private LoanStatus status;

    private BigDecimal loanFee;
    private Double parentClubSalaryShare;
    private Double loanClubSalaryShare;

    private Boolean hasRecallClause;
    private LocalDate earliestRecallDate;
    private Boolean hasOptionToBuy;
    private BigDecimal optionToBuyPrice;
    private Boolean hasObligationToBuy;
    private String obligationConditions;

    private Integer minimumAppearances;
    private Integer actualAppearances;
    private Boolean developmentTargetsMet;
    private String developmentTargets;

    private String loanReason;
    private String specialConditions;
    private LocalDateTime agreementDate;
}
