package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.FinancialReport;
import com.lollito.fm.model.FinancialTransaction;
import com.lollito.fm.model.ReportType;
import com.lollito.fm.model.SponsorshipDeal;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.model.rest.FinancialDashboardDTO;
import com.lollito.fm.model.rest.FinancialReportDTO;
import com.lollito.fm.model.rest.FinancialTransactionDTO;
import com.lollito.fm.model.rest.GenerateReportRequest;
import com.lollito.fm.model.rest.SponsorshipDealDTO;
import com.lollito.fm.service.FinancialService;

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
        return ResponseEntity.ok(convertToTransactionDTO(transaction));
    }

    @GetMapping("/club/{clubId}/transactions")
    public ResponseEntity<Page<FinancialTransactionDTO>> getTransactions(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionCategory category) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<FinancialTransaction> transactions = financialService
            .getClubTransactions(clubId, pageRequest, type, category);
        return ResponseEntity.ok(transactions.map(this::convertToTransactionDTO));
    }

    @GetMapping("/club/{clubId}/reports")
    public ResponseEntity<List<FinancialReportDTO>> getFinancialReports(
            @PathVariable Long clubId,
            @RequestParam(required = false) ReportType reportType) {
        List<FinancialReport> reports = financialService.getFinancialReports(clubId, reportType);
        return ResponseEntity.ok(reports.stream()
            .map(this::convertToReportDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/club/{clubId}/report/generate")
    public ResponseEntity<FinancialReportDTO> generateReport(
            @PathVariable Long clubId,
            @RequestBody GenerateReportRequest request) {
        FinancialReport report = financialService.generateCustomReport(clubId, request);
        return ResponseEntity.ok(convertToReportDTO(report));
    }

    @GetMapping("/club/{clubId}/sponsorships")
    public ResponseEntity<List<SponsorshipDealDTO>> getSponsorshipDeals(@PathVariable Long clubId) {
        List<SponsorshipDeal> deals = financialService.getClubSponsorships(clubId);
        return ResponseEntity.ok(deals.stream()
            .map(this::convertToSponsorshipDealDTO)
            .collect(Collectors.toList()));
    }

    // Conversion helpers

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

    private FinancialReportDTO convertToReportDTO(FinancialReport r) {
        return FinancialReportDTO.builder()
            .id(r.getId())
            .reportType(r.getReportType())
            .reportPeriodStart(r.getReportPeriodStart())
            .reportPeriodEnd(r.getReportPeriodEnd())
            .generatedDate(r.getGeneratedDate())
            .totalIncome(r.getTotalIncome())
            .totalExpenses(r.getTotalExpenses())
            .netProfit(r.getNetProfit())
            .cashFlow(r.getCashFlow())
            .incomeBreakdown(r.getIncomeBreakdown())
            .expenseBreakdown(r.getExpenseBreakdown())
            .reportSummary(r.getReportSummary())
            .recommendations(r.getRecommendations())
            .build();
    }

    private SponsorshipDealDTO convertToSponsorshipDealDTO(SponsorshipDeal s) {
        return SponsorshipDealDTO.builder()
            .id(s.getId())
            .sponsorName(s.getSponsorName())
            .sponsorLogo(s.getSponsorLogo())
            .type(s.getType())
            .annualValue(s.getAnnualValue())
            .totalValue(s.getTotalValue())
            .startDate(s.getStartDate())
            .endDate(s.getEndDate())
            .status(s.getStatus())
            .build();
    }
}
