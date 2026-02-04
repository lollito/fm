package com.lollito.fm.repository.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Finance;
import com.lollito.fm.model.FinancialTransaction;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findByFinanceAndEffectiveDateBetween(Finance finance, LocalDate startDate, LocalDate endDate);

    List<FinancialTransaction> findTop10ByFinanceOrderByTransactionDateDesc(Finance finance);

    Page<FinancialTransaction> findByFinance(Finance finance, Pageable pageable);

    Page<FinancialTransaction> findByFinanceAndType(Finance finance, TransactionType type, Pageable pageable);

    Page<FinancialTransaction> findByFinanceAndCategory(Finance finance, TransactionCategory category, Pageable pageable);

    Page<FinancialTransaction> findByFinanceAndTypeAndCategory(Finance finance, TransactionType type, TransactionCategory category, Pageable pageable);
}
