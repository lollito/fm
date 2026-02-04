package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialDashboardDTO {
    private BigDecimal currentBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal netProfit;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netWorth;
    private BigDecimal debt;
    private Double profitMargin;

    // Revenue breakdown
    private BigDecimal matchdayRevenue;
    private BigDecimal sponsorshipRevenue;
    private BigDecimal merchandiseRevenue;
    private BigDecimal transferRevenue;
    private BigDecimal prizeMoneyRevenue;
    private BigDecimal tvRightsRevenue;

    // Expense breakdown
    private BigDecimal playerSalaries;
    private BigDecimal staffSalaries;
    private BigDecimal facilityMaintenance;
    private BigDecimal transferExpenses;
    private BigDecimal operationalCosts;

    private List<FinancialTransactionDTO> recentTransactions;
}
