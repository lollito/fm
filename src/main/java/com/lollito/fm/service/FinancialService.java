package com.lollito.fm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.FinancialReport;
import com.lollito.fm.model.FinancialTransaction;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.ReportType;
import com.lollito.fm.model.SponsorshipDeal;
import com.lollito.fm.model.SponsorshipStatus;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionStatus;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.model.rest.FinancialDashboardDTO;
import com.lollito.fm.model.rest.FinancialTransactionDTO;
import com.lollito.fm.model.rest.GenerateReportRequest;
import com.lollito.fm.repository.rest.FinanceRepository;
import com.lollito.fm.repository.rest.FinancialReportRepository;
import com.lollito.fm.repository.rest.FinancialTransactionRepository;
import com.lollito.fm.repository.rest.SponsorshipDealRepository;

@Service
public class FinancialService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FinanceRepository financeRepository;

    @Autowired
    private FinancialTransactionRepository transactionRepository;

    @Autowired
    private FinancialReportRepository reportRepository;

    @Autowired
    private SponsorshipDealRepository sponsorshipRepository;

    @Autowired
    private ClubService clubService;

    @Autowired
    private SeasonService seasonService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public FinancialTransaction processTransaction(Long clubId, CreateTransactionRequest request) {
        Club club = clubService.findById(clubId);
        Finance finance = club.getFinance();

        if (finance == null) {
            // Should not happen if data is consistent, but handle it
            finance = new Finance();
            finance.setClub(club);
            club.setFinance(finance);
            // Club is the owner of the relationship, so we must save the club
            // Since CascadeType.ALL is set on Club.finance, saving club will save finance
            clubService.save(club);
            // Refetch finance from club to ensure it's managed/synced
            finance = club.getFinance();
        }

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
            .recurringMonths(request.getRecurringMonths())
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
        updateFinancialHealthIndicators(finance);
        financeRepository.save(finance);

        // Check for financial alerts
        checkFinancialAlerts(finance);

        return transaction;
    }

    @Scheduled(cron = "0 0 8 1 * *") // First day of month at 8 AM
    @Transactional
    public void processMonthlyFinancials() {
        List<Club> allClubs = clubService.findAll();

        for (Club club : allClubs) {
            try {
                processClubMonthlyFinancials(club);
            } catch (Exception e) {
                logger.error("Error processing monthly financials for club " + club.getId(), e);
            }
        }
    }

    @Transactional
    public void processClubMonthlyFinancials(Club club) {
        Finance finance = club.getFinance();
        if (finance == null) return;

        // Process recurring transactions logic could be added here if needed,
        // but for now we focus on salaries and other fixed costs.

        // Process player salaries
        processPlayerSalaries(club);

        // Process staff salaries
        processStaffSalaries(club);

        // Process sponsorship payments
        processSponsorshipPayments(club);

        // Process facility maintenance costs
        // Delegated to InfrastructureService to handle detailed facility maintenance
        // processFacilityMaintenance(club);

        // Generate monthly report
        generateMonthlyReport(finance);

        // Update financial health indicators
        updateFinancialHealthIndicators(finance);
        financeRepository.save(finance);
    }

    private void processPlayerSalaries(Club club) {
        List<Player> players = club.getTeam().getPlayers();

        for (Player player : players) {
            // Assuming salary is annual
            if (player.getSalary() != null) {
                BigDecimal monthlySalary = player.getSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

                processTransaction(club.getId(), CreateTransactionRequest.builder()
                    .type(TransactionType.EXPENSE)
                    .category(TransactionCategory.PLAYER_SALARIES)
                    .amount(monthlySalary)
                    .description("Monthly salary for " + player.getName() + " " + player.getSurname())
                    .reference("PLAYER_" + player.getId())
                    .effectiveDate(LocalDate.now())
                    .isRecurring(false)
                    .build());
            }
        }
    }

    private void processStaffSalaries(Club club) {
        // Placeholder: Assume generic staff cost if no detailed staff model
        BigDecimal estimatedStaffCost = BigDecimal.valueOf(50000);
        processTransaction(club.getId(), CreateTransactionRequest.builder()
            .type(TransactionType.EXPENSE)
            .category(TransactionCategory.STAFF_SALARIES)
            .amount(estimatedStaffCost)
            .description("Monthly staff salaries")
            .reference("STAFF_SALARIES")
            .effectiveDate(LocalDate.now())
            .isRecurring(false)
            .build());
    }

    private void processSponsorshipPayments(Club club) {
        List<SponsorshipDeal> deals = sponsorshipRepository.findByClub(club);
        for (SponsorshipDeal deal : deals) {
            if (deal.getStatus() == SponsorshipStatus.ACTIVE &&
                (deal.getEndDate() == null || deal.getEndDate().isAfter(LocalDate.now()))) {

                // Assuming monthly payments for annual value
                BigDecimal monthlyPayment = deal.getCurrentAnnualValue().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

                processTransaction(club.getId(), CreateTransactionRequest.builder()
                    .type(TransactionType.INCOME)
                    .category(TransactionCategory.SPONSORSHIP)
                    .amount(monthlyPayment)
                    .description("Sponsorship payment: " + (deal.getSponsor() != null ? deal.getSponsor().getName() : "Unknown"))
                    .reference("SPONSOR_" + deal.getId())
                    .effectiveDate(LocalDate.now())
                    .isRecurring(false)
                    .build());
            }
        }
    }

    private void processFacilityMaintenance(Club club) {
        Stadium stadium = club.getStadium();
        if (stadium != null) {
            // Simple calculation: capacity * 0.5 per month
            BigDecimal maintenanceCost = BigDecimal.valueOf(stadium.getCapacity()).multiply(BigDecimal.valueOf(0.5));

             processTransaction(club.getId(), CreateTransactionRequest.builder()
                .type(TransactionType.EXPENSE)
                .category(TransactionCategory.FACILITY_MAINTENANCE)
                .amount(maintenanceCost)
                .description("Stadium maintenance: " + stadium.getName())
                .reference("STADIUM_" + stadium.getId())
                .effectiveDate(LocalDate.now())
                .isRecurring(false)
                .build());
        }
    }

    @Transactional
    public void generateMatchdayRevenue(Match match) {
        Club homeClub = match.getHome();
        Stadium stadium = homeClub.getStadium();
        if (stadium == null) return;

        // Calculate attendance-based revenue (simplified)
        // Assume random attendance between 50% and 100% of capacity
        double attendanceFactor = 0.5 + (Math.random() * 0.5);
        int attendance = (int) (stadium.getCapacity() * attendanceFactor);

        // Average ticket price
        BigDecimal avgTicketPrice = BigDecimal.valueOf(30);
        BigDecimal ticketRevenue = BigDecimal.valueOf(attendance).multiply(avgTicketPrice);

        // Merchandise and concessions (simplified)
        BigDecimal merchandiseRevenue = BigDecimal.valueOf(attendance).multiply(BigDecimal.valueOf(5));

        BigDecimal totalMatchdayRevenue = ticketRevenue.add(merchandiseRevenue);

        processTransaction(homeClub.getId(), CreateTransactionRequest.builder()
            .type(TransactionType.INCOME)
            .category(TransactionCategory.MATCHDAY_REVENUE)
            .amount(totalMatchdayRevenue)
            .description("Matchday revenue vs " + match.getAway().getName())
            .reference("MATCH_" + match.getId())
            .effectiveDate(match.getDate().toLocalDate()) // Using LocalDateTime to LocalDate
            .isRecurring(false)
            .build());
    }

    @Transactional
    public FinancialReport generateMonthlyReport(Finance finance) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<FinancialTransaction> monthlyTransactions = transactionRepository
            .findByFinanceAndEffectiveDateBetween(finance, startOfMonth, endOfMonth);

        BigDecimal totalIncome = monthlyTransactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(FinancialTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = monthlyTransactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(FinancialTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalIncome.subtract(totalExpenses);

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

    @Transactional
    public FinancialDashboardDTO getFinancialDashboard(Long clubId) {
        Club club = clubService.findById(clubId);
        Finance finance = club.getFinance();
        if (finance == null) {
            // Return empty dashboard or initialize finance
             finance = new Finance();
             finance.setClub(club);
             club.setFinance(finance);
             clubService.save(club);
             finance = club.getFinance();
        }

        List<FinancialTransaction> recentTransactions = transactionRepository
            .findTop10ByFinanceOrderByTransactionDateDesc(finance);

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

        List<FinancialTransactionDTO> transactionDTOs = recentTransactions.stream()
                .map(this::convertToTransactionDTO)
                .collect(Collectors.toList());

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
            .matchdayRevenue(finance.getMatchdayRevenue())
            .sponsorshipRevenue(finance.getSponsorshipRevenue())
            .merchandiseRevenue(finance.getMerchandiseRevenue())
            .transferRevenue(finance.getTransferRevenue())
            .prizeMoneyRevenue(finance.getPrizeMoneyRevenue())
            .tvRightsRevenue(finance.getTvRightsRevenue())
            .playerSalaries(finance.getPlayerSalaries())
            .staffSalaries(finance.getStaffSalaries())
            .facilityMaintenance(finance.getFacilityMaintenance())
            .transferExpenses(finance.getTransferExpenses())
            .operationalCosts(finance.getOperationalCosts())
            .recentTransactions(transactionDTOs)
            .build();
    }

    public Page<FinancialTransaction> getClubTransactions(Long clubId, Pageable pageable, TransactionType type, TransactionCategory category) {
        Club club = clubService.findById(clubId);
        Finance finance = club.getFinance();

        if (type != null && category != null) {
            return transactionRepository.findByFinanceAndTypeAndCategory(finance, type, category, pageable);
        } else if (type != null) {
            return transactionRepository.findByFinanceAndType(finance, type, pageable);
        } else if (category != null) {
            return transactionRepository.findByFinanceAndCategory(finance, category, pageable);
        } else {
            return transactionRepository.findByFinance(finance, pageable);
        }
    }

    public List<FinancialReport> getFinancialReports(Long clubId, ReportType reportType) {
        Club club = clubService.findById(clubId);
        Finance finance = club.getFinance();

        if (reportType != null) {
            return reportRepository.findByFinanceAndReportType(finance, reportType);
        } else {
            return reportRepository.findByFinance(finance);
        }
    }

    public FinancialReport generateCustomReport(Long clubId, GenerateReportRequest request) {
        // Simple implementation - could be more complex
        Club club = clubService.findById(clubId);
        return generateMonthlyReport(club.getFinance());
    }

    public List<SponsorshipDeal> getClubSponsorships(Long clubId) {
        Club club = clubService.findById(clubId);
        return sponsorshipRepository.findByClub(club);
    }

    // Helpers

    private void updateRevenueCategory(Finance finance, TransactionCategory category, BigDecimal amount) {
        switch (category) {
            case MATCHDAY_REVENUE -> finance.setMatchdayRevenue(finance.getMatchdayRevenue().add(amount));
            case SPONSORSHIP -> finance.setSponsorshipRevenue(finance.getSponsorshipRevenue().add(amount));
            case MERCHANDISE -> finance.setMerchandiseRevenue(finance.getMerchandiseRevenue().add(amount));
            case TRANSFER_INCOME -> finance.setTransferRevenue(finance.getTransferRevenue().add(amount));
            case PRIZE_MONEY -> finance.setPrizeMoneyRevenue(finance.getPrizeMoneyRevenue().add(amount));
            case TV_RIGHTS -> finance.setTvRightsRevenue(finance.getTvRightsRevenue().add(amount));
            default -> {} // Other categories
        }
        finance.setTotalRevenue(finance.getTotalRevenue().add(amount));
    }

    private void updateExpenseCategory(Finance finance, TransactionCategory category, BigDecimal amount) {
        switch (category) {
            case PLAYER_SALARIES -> finance.setPlayerSalaries(finance.getPlayerSalaries().add(amount));
            case STAFF_SALARIES -> finance.setStaffSalaries(finance.getStaffSalaries().add(amount));
            case TRANSFER_FEES -> finance.setTransferExpenses(finance.getTransferExpenses().add(amount));
            case FACILITY_MAINTENANCE -> finance.setFacilityMaintenance(finance.getFacilityMaintenance().add(amount));
            case OPERATIONAL_COSTS -> finance.setOperationalCosts(finance.getOperationalCosts().add(amount));
            default -> {}
        }
        finance.setTotalExpenses(finance.getTotalExpenses().add(amount));
    }

    private void updateFinancialHealthIndicators(Finance finance) {
        // Simplified net worth calculation (assets - liabilities)
        // Here we just use balance as current asset
        finance.setNetWorth(finance.getBalance());

        // Debt is not fully tracked yet, assume 0 or accumulated loans
        // finance.setDebt(...)

        if (finance.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = finance.getTotalRevenue().subtract(finance.getTotalExpenses());
            double margin = profit.divide(finance.getTotalRevenue(), 4, RoundingMode.HALF_UP).doubleValue();
            finance.setProfitMargin(margin);
        } else {
            finance.setProfitMargin(0.0);
        }
    }

    private void checkFinancialAlerts(Finance finance) {
         if (finance.getBalance().compareTo(BigDecimal.valueOf(100000)) < 0) {
             String message = "Low Balance Alert: Club balance is below $100,000 for club " + finance.getClub().getName();
             logger.warn(message);
             newsService.save(new com.lollito.fm.model.News(message, LocalDateTime.now()));
         }

         if (finance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
             String message = "Negative Balance Alert: Club is in debt: " + finance.getClub().getName();
             logger.error(message);
             newsService.save(new com.lollito.fm.model.News(message, LocalDateTime.now()));
         }
    }

    private String generateIncomeBreakdown(List<FinancialTransaction> transactions) {
        Map<TransactionCategory, BigDecimal> breakdown = new HashMap<>();
        for (FinancialTransaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                breakdown.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }
        try {
            return objectMapper.writeValueAsString(breakdown);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String generateExpenseBreakdown(List<FinancialTransaction> transactions) {
        Map<TransactionCategory, BigDecimal> breakdown = new HashMap<>();
        for (FinancialTransaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                breakdown.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }
         try {
            return objectMapper.writeValueAsString(breakdown);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String generateReportSummary(BigDecimal income, BigDecimal expenses, BigDecimal profit) {
        return String.format("Total Income: %s, Total Expenses: %s, Net Profit: %s", income, expenses, profit);
    }

    private FinancialTransactionDTO convertToTransactionDTO(FinancialTransaction t) {
        return FinancialTransactionDTO.builder()
            .id(t.getId())
            .type(t.getType())
            .category(t.getCategory())
            .amount(t.getAmount())
            .description(t.getDescription())
            .reference(t.getReference())
            .transactionDate(t.getTransactionDate())
            .effectiveDate(t.getEffectiveDate())
            .status(t.getStatus())
            .notes(t.getNotes())
            .build();
    }
}
