# Financial Management System Implementation

## Overview
Implement a comprehensive financial management system with detailed budget tracking, revenue streams, expense management, and financial reporting for football clubs.

## Technical Requirements

### Database Schema Changes

#### Enhanced Finance Entity
```java
@Entity
@Table(name = "finance")
public class Finance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "finance")
    private Club club;
    
    // Current financial status
    private BigDecimal balance;
    private BigDecimal monthlyBudget;
    private BigDecimal seasonBudget;
    
    // Revenue tracking
    private BigDecimal totalRevenue;
    private BigDecimal matchdayRevenue;
    private BigDecimal sponsorshipRevenue;
    private BigDecimal merchandiseRevenue;
    private BigDecimal transferRevenue;
    private BigDecimal prizeMoneyRevenue;
    private BigDecimal tvRightsRevenue;
    
    // Expense tracking
    private BigDecimal totalExpenses;
    private BigDecimal playerSalaries;
    private BigDecimal staffSalaries;
    private BigDecimal facilityMaintenance;
    private BigDecimal transferExpenses;
    private BigDecimal operationalCosts;
    private BigDecimal loanInterest;
    
    // Financial health indicators
    private BigDecimal netWorth;
    private BigDecimal debt;
    private Double profitMargin;
    private LocalDate lastUpdated;
    
    @OneToMany(mappedBy = "finance", cascade = CascadeType.ALL)
    private List<FinancialTransaction> transactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "finance", cascade = CascadeType.ALL)
    private List<FinancialReport> reports = new ArrayList<>();
}
```

#### New Entity: FinancialTransaction
```java
@Entity
@Table(name = "financial_transaction")
public class FinancialTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_id")
    private Finance finance;
    
    @Enumerated(EnumType.STRING)
    private TransactionType type; // INCOME, EXPENSE
    
    @Enumerated(EnumType.STRING)
    private TransactionCategory category;
    
    private BigDecimal amount;
    private String description;
    private String reference; // Reference to related entity (player, match, etc.)
    
    private LocalDateTime transactionDate;
    private LocalDate effectiveDate;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // PENDING, COMPLETED, CANCELLED
    
    private String notes;
    private Boolean isRecurring;
    private Integer recurringMonths;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
}
```

#### New Entity: FinancialReport
```java
@Entity
@Table(name = "financial_report")
public class FinancialReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_id")
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
    private String incomeBreakdown;
    private String expenseBreakdown;
    private String comparisonData; // Comparison with previous periods
    
    private String reportSummary;
    private String recommendations;
}
```

#### New Entity: SponsorshipDeal
```java
@Entity
@Table(name = "sponsorship_deal")
public class SponsorshipDeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    private String sponsorName;
    private String sponsorLogo;
    
    @Enumerated(EnumType.STRING)
    private SponsorshipType type; // SHIRT, STADIUM, TRAINING_GROUND, GENERAL
    
    private BigDecimal annualValue;
    private BigDecimal totalValue;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status; // ACTIVE, EXPIRED, TERMINATED
    
    // Performance bonuses
    private BigDecimal leaguePositionBonus;
    private BigDecimal cupProgressBonus;
    private BigDecimal attendanceBonus;
    
    private String contractTerms;
    private Boolean autoRenewal;
    private Integer renewalYears;
}
```

#### Enums to Create
```java
public enum TransactionType {
    INCOME, EXPENSE
}

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
    INSURANCE("Insurance");
    
    private final String displayName;
}

public enum ReportType {
    MONTHLY("Monthly Report"),
    QUARTERLY("Quarterly Report"),
    ANNUAL("Annual Report"),
    SEASON("Season Report");
    
    private final String displayName;
}

public enum SponsorshipType {
    SHIRT("Shirt Sponsor"),
    STADIUM("Stadium Naming Rights"),
    TRAINING_GROUND("Training Ground"),
    GENERAL("General Sponsor");
    
    private final String displayName;
}

public enum SponsorshipStatus {
    ACTIVE, EXPIRED, TERMINATED
}

public enum TransactionStatus {
    PENDING, COMPLETED, CANCELLED
}
```

### Service Layer Implementation

#### FinancialService
```java
@Service
public class FinancialService {
    
    @Autowired
    private FinanceRepository financeRepository;
    
    @Autowired
    private FinancialTransactionRepository transactionRepository;
    
    @Autowired
    private FinancialReportRepository reportRepository;
    
    @Autowired
    private SponsorshipDealRepository sponsorshipRepository;
    
    /**
     * Process financial transaction
     */
    public FinancialTransaction processTransaction(Long clubId, CreateTransactionRequest request) {
        Club club = clubService.findById(clubId);
        Finance finance = club.getFinance();
        
        FinancialTransaction transaction = FinancialTransaction.builder()
            .finance(finance)
            .type(request.getType())
            .category(request.getCategory())
            .amount(request.getAmount())
            .description(request.getDescription())
            .reference(request.getReference())
            .transactionDate(LocalDateTime.now())
            .effectiveDate(request.getEffectiveDate())
            .status(TransactionStatus.COMPLETED)
            .notes(request.getNotes())
            .isRecurring(request.getIsRecurring())
            .season(seasonService.getCurrentSeason())
            .build();
            
        transaction = transactionRepository.save(transaction);
        
        // Update finance balance
        if (request.getType() == TransactionType.INCOME) {
            finance.setBalance(finance.getBalance().add(request.getAmount()));
            updateRevenueCategory(finance, request.getCategory(), request.getAmount());
        } else {
            finance.setBalance(finance.getBalance().subtract(request.getAmount()));
            updateExpenseCategory(finance, request.getCategory(), request.getAmount());
        }
        
        finance.setLastUpdated(LocalDate.now());
        financeRepository.save(finance);
        
        // Check for financial alerts
        checkFinancialAlerts(finance);
        
        return transaction;
    }
    
    /**
     * Process monthly financial operations
     */
    @Scheduled(cron = "0 0 8 1 * *") // First day of month at 8 AM
    public void processMonthlyFinancials() {
        List<Club> allClubs = clubService.findAll();
        
        for (Club club : allClubs) {
            processClubMonthlyFinancials(club);
        }
    }
    
    /**
     * Process monthly financials for a club
     */
    private void processClubMonthlyFinancials(Club club) {
        Finance finance = club.getFinance();
        
        // Process recurring transactions
        processRecurringTransactions(finance);
        
        // Process player salaries
        processPlayerSalaries(club);
        
        // Process staff salaries
        processStaffSalaries(club);
        
        // Process sponsorship payments
        processSponsorshipPayments(club);
        
        // Process facility maintenance costs
        processFacilityMaintenance(club);
        
        // Generate monthly report
        generateMonthlyReport(finance);
        
        // Update financial health indicators
        updateFinancialHealthIndicators(finance);
    }
    
    /**
     * Process player salaries
     */
    private void processPlayerSalaries(Club club) {
        List<Player> players = club.getTeam().getPlayers();
        BigDecimal totalSalaries = BigDecimal.ZERO;
        
        for (Player player : players) {
            if (player.getSalary() != null) {
                BigDecimal monthlySalary = player.getSalary().divide(BigDecimal.valueOf(12), 
                                                                   RoundingMode.HALF_UP);
                totalSalaries = totalSalaries.add(monthlySalary);
                
                // Create individual salary transaction
                processTransaction(club.getId(), CreateTransactionRequest.builder()
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.PLAYER_SALARIES)
                    .amount(monthlySalary)
                    .description("Monthly salary for " + player.getName() + " " + player.getSurname())
                    .reference("PLAYER_" + player.getId())
                    .effectiveDate(LocalDate.now())
                    .build());
            }
        }
    }
    
    /**
     * Generate matchday revenue
     */
    public void generateMatchdayRevenue(Match match) {
        Club homeClub = match.getHomeTeam().getClub();
        Stadium stadium = homeClub.getStadium();
        
        // Calculate attendance-based revenue
        int attendance = calculateAttendance(match, stadium);
        BigDecimal ticketRevenue = calculateTicketRevenue(attendance, stadium);
        BigDecimal merchandiseRevenue = calculateMatchdayMerchandise(attendance);
        BigDecimal concessionRevenue = calculateConcessionRevenue(attendance);
        
        BigDecimal totalMatchdayRevenue = ticketRevenue
            .add(merchandiseRevenue)
            .add(concessionRevenue);
        
        // Process matchday revenue transaction
        processTransaction(homeClub.getId(), CreateTransactionRequest.builder()
            .type(TransactionType.INCOME)
            .category(TransactionCategory.MATCHDAY_REVENUE)
            .amount(totalMatchdayRevenue)
            .description("Matchday revenue vs " + match.getAwayTeam().getClub().getName())
            .reference("MATCH_" + match.getId())
            .effectiveDate(match.getMatchDate().toLocalDate())
            .build());
    }
    
    /**
     * Generate financial report
     */
    public FinancialReport generateMonthlyReport(Finance finance) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        // Get transactions for the month
        List<FinancialTransaction> monthlyTransactions = transactionRepository
            .findByFinanceAndEffectiveDateBetween(finance, startOfMonth, endOfMonth);
        
        // Calculate totals
        BigDecimal totalIncome = monthlyTransactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(FinancialTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalExpenses = monthlyTransactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(FinancialTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal netProfit = totalIncome.subtract(totalExpenses);
        
        // Create report
        FinancialReport report = FinancialReport.builder()
            .finance(finance)
            .reportType(ReportType.MONTHLY)
            .reportPeriodStart(startOfMonth)
            .reportPeriodEnd(endOfMonth)
            .generatedDate(LocalDate.now())
            .totalIncome(totalIncome)
            .totalExpenses(totalExpenses)
            .netProfit(netProfit)
            .cashFlow(finance.getBalance())
            .incomeBreakdown(generateIncomeBreakdown(monthlyTransactions))
            .expenseBreakdown(generateExpenseBreakdown(monthlyTransactions))
            .reportSummary(generateReportSummary(totalIncome, totalExpenses, netProfit))
            .build();
            
        return reportRepository.save(report);
    }
    
    /**
     * Get financial dashboard data
     */
    public FinancialDashboardDTO getFinancialDashboard(Long clubId) {
        Club club = clubService.findById(clubId);
        Finance finance = club.getFinance();
        
        // Get recent transactions
        List<FinancialTransaction> recentTransactions = transactionRepository
            .findTop10ByFinanceOrderByTransactionDateDesc(finance);
        
        // Get current month data
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        
        List<FinancialTransaction> monthlyTransactions = transactionRepository
            .findByFinanceAndEffectiveDateBetween(finance, startOfMonth, now);
        
        BigDecimal monthlyIncome = monthlyTransactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(FinancialTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal monthlyExpenses = monthlyTransactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(FinancialTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return FinancialDashboardDTO.builder()
            .currentBalance(finance.getBalance())
            .monthlyIncome(monthlyIncome)
            .monthlyExpenses(monthlyExpenses)
            .netProfit(monthlyIncome.subtract(monthlyExpenses))
            .totalRevenue(finance.getTotalRevenue())
            .totalExpenses(finance.getTotalExpenses())
            .netWorth(finance.getNetWorth())
            .debt(finance.getDebt())
            .profitMargin(finance.getProfitMargin())
            .recentTransactions(recentTransactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()))
            .build();
    }
    
    private void updateRevenueCategory(Finance finance, TransactionCategory category, 
                                     BigDecimal amount) {
        switch (category) {
            case MATCHDAY_REVENUE -> finance.setMatchdayRevenue(
                finance.getMatchdayRevenue().add(amount));
            case SPONSORSHIP -> finance.setSponsorshipRevenue(
                finance.getSponsorshipRevenue().add(amount));
            case MERCHANDISE -> finance.setMerchandiseRevenue(
                finance.getMerchandiseRevenue().add(amount));
            case TRANSFER_INCOME -> finance.setTransferRevenue(
                finance.getTransferRevenue().add(amount));
            case PRIZE_MONEY -> finance.setPrizeMoneyRevenue(
                finance.getPrizeMoneyRevenue().add(amount));
            case TV_RIGHTS -> finance.setTvRightsRevenue(
                finance.getTvRightsRevenue().add(amount));
        }
        
        finance.setTotalRevenue(finance.getTotalRevenue().add(amount));
    }
    
    private void checkFinancialAlerts(Finance finance) {
        // Low balance alert
        if (finance.getBalance().compareTo(BigDecimal.valueOf(100000)) < 0) {
            createFinancialAlert(finance, "Low Balance", 
                               "Club balance is below $100,000", AlertSeverity.WARNING);
        }
        
        // Negative balance alert
        if (finance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            createFinancialAlert(finance, "Negative Balance", 
                               "Club is in debt", AlertSeverity.CRITICAL);
        }
        
        // High expense ratio alert
        if (finance.getProfitMargin() != null && finance.getProfitMargin() < -0.2) {
            createFinancialAlert(finance, "High Expenses", 
                               "Expenses exceed income by more than 20%", AlertSeverity.WARNING);
        }
    }
}
```

### API Endpoints

#### FinancialController
```java
@RestController
@RequestMapping("/api/finance")
public class FinancialController {
    
    @Autowired
    private FinancialService financialService;
    
    @GetMapping("/club/{clubId}/dashboard")
    public ResponseEntity<FinancialDashboardDTO> getFinancialDashboard(@PathVariable Long clubId) {
        FinancialDashboardDTO dashboard = financialService.getFinancialDashboard(clubId);
        return ResponseEntity.ok(dashboard);
    }
    
    @PostMapping("/club/{clubId}/transaction")
    public ResponseEntity<FinancialTransactionDTO> createTransaction(
            @PathVariable Long clubId,
            @RequestBody CreateTransactionRequest request) {
        FinancialTransaction transaction = financialService.processTransaction(clubId, request);
        return ResponseEntity.ok(convertToDTO(transaction));
    }
    
    @GetMapping("/club/{clubId}/transactions")
    public ResponseEntity<Page<FinancialTransactionDTO>> getTransactions(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionCategory category) {
        Page<FinancialTransaction> transactions = financialService
            .getClubTransactions(clubId, PageRequest.of(page, size), type, category);
        return ResponseEntity.ok(transactions.map(this::convertToDTO));
    }
    
    @GetMapping("/club/{clubId}/reports")
    public ResponseEntity<List<FinancialReportDTO>> getFinancialReports(
            @PathVariable Long clubId,
            @RequestParam(required = false) ReportType reportType) {
        List<FinancialReport> reports = financialService.getFinancialReports(clubId, reportType);
        return ResponseEntity.ok(reports.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/club/{clubId}/report/generate")
    public ResponseEntity<FinancialReportDTO> generateReport(
            @PathVariable Long clubId,
            @RequestBody GenerateReportRequest request) {
        FinancialReport report = financialService.generateCustomReport(clubId, request);
        return ResponseEntity.ok(convertToDTO(report));
    }
    
    @GetMapping("/club/{clubId}/sponsorships")
    public ResponseEntity<List<SponsorshipDealDTO>> getSponsorshipDeals(@PathVariable Long clubId) {
        List<SponsorshipDeal> deals = financialService.getClubSponsorships(clubId);
        return ResponseEntity.ok(deals.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### FinancialDashboard Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getFinancialDashboard, getTransactions, getFinancialReports } from '../services/api';
import { Line, Pie, Bar } from 'react-chartjs-2';

const FinancialDashboard = ({ clubId }) => {
    const [dashboard, setDashboard] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [selectedPeriod, setSelectedPeriod] = useState('month');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadFinancialData();
    }, [clubId, selectedPeriod]);

    const loadFinancialData = async () => {
        try {
            const [dashboardResponse, transactionsResponse] = await Promise.all([
                getFinancialDashboard(clubId),
                getTransactions(clubId, 0, 50)
            ]);
            
            setDashboard(dashboardResponse.data);
            setTransactions(transactionsResponse.data.content);
        } catch (error) {
            console.error('Error loading financial data:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    };

    const getBalanceColor = (balance) => {
        if (balance >= 1000000) return '#4caf50'; // Green for healthy
        if (balance >= 100000) return '#ff9800';  // Orange for warning
        return '#f44336'; // Red for critical
    };

    if (loading) return <div>Loading financial dashboard...</div>;

    return (
        <div className="financial-dashboard">
            <div className="dashboard-header">
                <h2>Financial Overview</h2>
                <div className="period-selector">
                    <select 
                        value={selectedPeriod}
                        onChange={(e) => setSelectedPeriod(e.target.value)}
                    >
                        <option value="month">This Month</option>
                        <option value="quarter">This Quarter</option>
                        <option value="year">This Year</option>
                    </select>
                </div>
            </div>

            <div className="financial-summary">
                <div className="summary-card balance">
                    <h3>Current Balance</h3>
                    <span 
                        className="amount"
                        style={{ color: getBalanceColor(dashboard.currentBalance) }}
                    >
                        {formatCurrency(dashboard.currentBalance)}
                    </span>
                </div>
                
                <div className="summary-card income">
                    <h3>Monthly Income</h3>
                    <span className="amount positive">
                        {formatCurrency(dashboard.monthlyIncome)}
                    </span>
                </div>
                
                <div className="summary-card expenses">
                    <h3>Monthly Expenses</h3>
                    <span className="amount negative">
                        {formatCurrency(dashboard.monthlyExpenses)}
                    </span>
                </div>
                
                <div className="summary-card profit">
                    <h3>Net Profit</h3>
                    <span 
                        className={`amount ${dashboard.netProfit >= 0 ? 'positive' : 'negative'}`}
                    >
                        {formatCurrency(dashboard.netProfit)}
                    </span>
                </div>
            </div>

            <div className="financial-charts">
                <div className="chart-container">
                    <h3>Revenue Breakdown</h3>
                    <Pie data={getRevenueChartData(dashboard)} />
                </div>
                
                <div className="chart-container">
                    <h3>Expense Breakdown</h3>
                    <Pie data={getExpenseChartData(dashboard)} />
                </div>
                
                <div className="chart-container full-width">
                    <h3>Cash Flow Trend</h3>
                    <Line data={getCashFlowChartData(dashboard)} />
                </div>
            </div>

            <div className="recent-transactions">
                <h3>Recent Transactions</h3>
                <div className="transactions-list">
                    {dashboard.recentTransactions.map(transaction => (
                        <div key={transaction.id} className="transaction-item">
                            <div className="transaction-info">
                                <span className="transaction-description">
                                    {transaction.description}
                                </span>
                                <span className="transaction-category">
                                    {transaction.category}
                                </span>
                                <span className="transaction-date">
                                    {new Date(transaction.transactionDate).toLocaleDateString()}
                                </span>
                            </div>
                            <div className="transaction-amount">
                                <span 
                                    className={`amount ${transaction.type === 'INCOME' ? 'positive' : 'negative'}`}
                                >
                                    {transaction.type === 'INCOME' ? '+' : '-'}
                                    {formatCurrency(transaction.amount)}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="financial-health">
                <h3>Financial Health Indicators</h3>
                <div className="health-indicators">
                    <div className="indicator">
                        <span>Net Worth:</span>
                        <span className="value">{formatCurrency(dashboard.netWorth)}</span>
                    </div>
                    <div className="indicator">
                        <span>Total Debt:</span>
                        <span className="value negative">{formatCurrency(dashboard.debt)}</span>
                    </div>
                    <div className="indicator">
                        <span>Profit Margin:</span>
                        <span className={`value ${dashboard.profitMargin >= 0 ? 'positive' : 'negative'}`}>
                            {(dashboard.profitMargin * 100).toFixed(1)}%
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

const getRevenueChartData = (dashboard) => ({
    labels: ['Matchday', 'Sponsorship', 'Merchandise', 'Transfers', 'Prize Money', 'TV Rights'],
    datasets: [{
        data: [
            dashboard.matchdayRevenue || 0,
            dashboard.sponsorshipRevenue || 0,
            dashboard.merchandiseRevenue || 0,
            dashboard.transferRevenue || 0,
            dashboard.prizeMoneyRevenue || 0,
            dashboard.tvRightsRevenue || 0
        ],
        backgroundColor: [
            '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'
        ]
    }]
});

const getExpenseChartData = (dashboard) => ({
    labels: ['Player Salaries', 'Staff Salaries', 'Transfers', 'Maintenance', 'Operations'],
    datasets: [{
        data: [
            dashboard.playerSalaries || 0,
            dashboard.staffSalaries || 0,
            dashboard.transferExpenses || 0,
            dashboard.facilityMaintenance || 0,
            dashboard.operationalCosts || 0
        ],
        backgroundColor: [
            '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'
        ]
    }]
});

export default FinancialDashboard;
```

## Implementation Notes

1. **Real-time Updates**: Financial data should update in real-time as transactions occur
2. **Automated Processing**: Monthly recurring transactions should be processed automatically
3. **Financial Alerts**: Implement alerts for low balance, high expenses, etc.
4. **Reporting**: Generate comprehensive financial reports for different periods
5. **Audit Trail**: Maintain complete transaction history for auditing
6. **Integration**: Connect with all systems that affect finances (transfers, salaries, etc.)

## Dependencies

- Player and Staff salary systems
- Transfer system for transfer fees
- Match system for matchday revenue
- Stadium system for capacity-based revenue
- Sponsorship system for recurring income
- Season system for period-based reporting