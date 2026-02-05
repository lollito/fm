package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateLoanProposalRequest {
    private Long playerId;
    private Long proposingClubId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal loanFee;
    private Double salaryShare;
    private Boolean recallClause;
    private Boolean optionToBuy;
    private BigDecimal optionPrice;
    private String message;
}
