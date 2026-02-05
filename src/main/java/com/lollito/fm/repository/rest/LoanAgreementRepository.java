package com.lollito.fm.repository.rest;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanStatus;
import com.lollito.fm.model.Club;

@Repository
public interface LoanAgreementRepository extends JpaRepository<LoanAgreement, Long> {
    List<LoanAgreement> findByStatus(LoanStatus status);
    List<LoanAgreement> findByParentClub(Club club);
    List<LoanAgreement> findByLoanClub(Club club);
    List<LoanAgreement> findByParentClubAndStatus(Club club, LoanStatus status);
    List<LoanAgreement> findByLoanClubAndStatus(Club club, LoanStatus status);
}
