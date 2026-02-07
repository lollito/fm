package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanStatus;

@Repository
public interface LoanAgreementRepository extends JpaRepository<LoanAgreement, Long> {
    @Query("SELECT l FROM LoanAgreement l JOIN FETCH l.player WHERE l.status = :status")
    List<LoanAgreement> findWithPlayerByStatus(@Param("status") LoanStatus status);

    List<LoanAgreement> findByStatus(LoanStatus status);
    List<LoanAgreement> findByParentClub(Club club);
    List<LoanAgreement> findByLoanClub(Club club);
    List<LoanAgreement> findByParentClubAndStatus(Club club, LoanStatus status);
    List<LoanAgreement> findByLoanClubAndStatus(Club club, LoanStatus status);
}
