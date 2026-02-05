package com.lollito.fm.model;

import lombok.Getter;

@Getter
public enum TransactionCategory {
    // Income categories
    MATCHDAY_REVENUE("Matchday Revenue"),
    SPONSORSHIP("Sponsorship"),
    MERCHANDISE("Merchandise"),
    TRANSFER_INCOME("Transfer Income"),
    PRIZE_MONEY("Prize Money"),
    TV_RIGHTS("TV Rights"),
    LOAN_INCOME("Loan Income"),

    // Expense categories
    PLAYER_SALARIES("Player Salaries"),
    STAFF_SALARIES("Staff Salaries"),
    TRANSFER_FEES("Transfer Fees"),
    FACILITY_MAINTENANCE("Facility Maintenance"),
    OPERATIONAL_COSTS("Operational Costs"),
    LOAN_PAYMENTS("Loan Payments"),
    TAXES("Taxes"),
    INSURANCE("Insurance"),
    SIGNING_BONUS("Signing Bonus"),
    SYSTEM_ACTION("System Action");

    private final String displayName;

    TransactionCategory(String displayName) {
        this.displayName = displayName;
    }
}
