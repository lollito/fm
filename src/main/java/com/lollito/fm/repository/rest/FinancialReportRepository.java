package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Finance;
import com.lollito.fm.model.FinancialReport;
import com.lollito.fm.model.ReportType;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, Long> {

    List<FinancialReport> findByFinance(Finance finance);

    List<FinancialReport> findByFinanceAndReportType(Finance finance, ReportType reportType);
}
