package com.lollito.fm.repository.rest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lollito.fm.model.LoanPerformanceReview;
import com.lollito.fm.model.LoanAgreement;

@Repository
public interface LoanPerformanceReviewRepository extends JpaRepository<LoanPerformanceReview, Long> {
    List<LoanPerformanceReview> findByLoanAgreement(LoanAgreement loanAgreement);
}
