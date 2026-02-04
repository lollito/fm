package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.ReportType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialReportDTO {
    private Long id;
    private ReportType reportType;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
    private LocalDate generatedDate;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal cashFlow;
    private String incomeBreakdown;
    private String expenseBreakdown;
    private String reportSummary;
    private String recommendations;
}
