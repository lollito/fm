package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.LoanProposal;
import com.lollito.fm.model.ProposalStatus;

@Repository
public interface LoanProposalRepository extends JpaRepository<LoanProposal, Long> {
    List<LoanProposal> findByTargetClubAndStatus(Club club, ProposalStatus status);
    List<LoanProposal> findByProposingClubAndStatus(Club club, ProposalStatus status);
}
