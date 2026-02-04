package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTransactionRequest {
    private TransactionType type;
    private TransactionCategory category;
    private BigDecimal amount;
    private String description;
    private String reference;
    private LocalDate effectiveDate;
    private String notes;
    private Boolean isRecurring;
    private Integer recurringMonths;
}
