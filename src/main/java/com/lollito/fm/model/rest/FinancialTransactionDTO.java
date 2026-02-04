package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionStatus;
import com.lollito.fm.model.TransactionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialTransactionDTO {
    private Long id;
    private TransactionType type;
    private TransactionCategory category;
    private BigDecimal amount;
    private String description;
    private String reference;
    private LocalDateTime transactionDate;
    private LocalDate effectiveDate;
    private TransactionStatus status;
    private String notes;
}
