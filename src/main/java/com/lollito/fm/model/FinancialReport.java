package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "financial_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FinancialReport implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_id")
    @ToString.Exclude
    private Finance finance;

    @Enumerated(EnumType.STRING)
    private ReportType reportType; // MONTHLY, QUARTERLY, ANNUAL, SEASON

    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
    private LocalDate generatedDate;

    // Financial summary
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal cashFlow;

    // Detailed breakdowns (JSON)
    @Column(length = 4000)
    private String incomeBreakdown;
    @Column(length = 4000)
    private String expenseBreakdown;
    @Column(length = 4000)
    private String comparisonData; // Comparison with previous periods

    @Column(length = 4000)
    private String reportSummary;
    @Column(length = 4000)
    private String recommendations;
}
