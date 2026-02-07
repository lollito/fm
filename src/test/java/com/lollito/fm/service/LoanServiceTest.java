package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanProposal;
import com.lollito.fm.model.LoanStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.ProposalStatus;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.TransactionType;
import com.lollito.fm.model.TransferType;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.LoanAgreementRepository;
import com.lollito.fm.repository.rest.LoanPerformanceReviewRepository;
import com.lollito.fm.repository.rest.LoanProposalRepository;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;

    @Mock private LoanAgreementRepository loanAgreementRepository;
    @Mock private LoanProposalRepository loanProposalRepository;
    @Mock private LoanPerformanceReviewRepository performanceReviewRepository;
    @Mock private PlayerService playerService;
    @Mock private ClubService clubService;
    @Mock private ClubRepository clubRepository;
    @Mock private FinancialService financialService;
    @Mock private PlayerHistoryService playerHistoryService;
    @Mock private SeasonService seasonService;
    @Mock private NewsService newsService;

    @Test
    public void acceptLoanProposal_Success() {
        Long proposalId = 1L;
        Long playerId = 100L;
        Long parentClubId = 200L;
        Long loanClubId = 300L;

        Player player = new Player();
        player.setId(playerId);

        Team parentTeam = new Team();
        parentTeam.setId(201L);
        Club parentClub = new Club();
        parentClub.setId(parentClubId);
        parentClub.setTeam(parentTeam);
        parentTeam.setClub(parentClub);
        player.setTeam(parentTeam);

        Team loanTeam = new Team();
        loanTeam.setId(301L);
        Club loanClub = new Club();
        loanClub.setId(loanClubId);
        loanClub.setTeam(loanTeam);
        loanTeam.setClub(loanClub);

        LoanProposal proposal = new LoanProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setPlayer(player);
        proposal.setTargetClub(parentClub);
        proposal.setProposingClub(loanClub);
        proposal.setProposedStartDate(LocalDate.now());
        proposal.setProposedEndDate(LocalDate.now().plusMonths(6));
        proposal.setProposedLoanFee(new BigDecimal("100000"));
        proposal.setProposedSalaryShare(0.5);
        proposal.setProposedRecallClause(true);
        proposal.setProposedOptionToBuy(false);

        when(loanProposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(loanAgreementRepository.save(any(LoanAgreement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanAgreement agreement = loanService.acceptLoanProposal(proposalId);

        // Verify agreement creation
        assertNotNull(agreement);
        assertEquals(LoanStatus.ACTIVE, agreement.getStatus());
        assertEquals(player, agreement.getPlayer());
        assertEquals(parentClub, agreement.getParentClub());
        assertEquals(loanClub, agreement.getLoanClub());
        assertEquals(proposal.getProposedLoanFee(), agreement.getLoanFee());

        // Verify proposal status update
        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
        assertNotNull(proposal.getResponseDate());
        verify(loanProposalRepository).save(proposal);

        // Verify player transfer
        assertEquals(loanTeam, player.getTeam());
        assertEquals(parentTeam, player.getOriginalTeam());
        verify(playerService).save(player);
        verify(playerHistoryService).recordTransfer(eq(player), eq(parentClub), eq(loanClub), eq(BigDecimal.ZERO), eq(TransferType.LOAN));

        // Verify financial transactions
        verify(financialService).processTransaction(eq(loanClubId), any(CreateTransactionRequest.class));
        verify(financialService).processTransaction(eq(parentClubId), any(CreateTransactionRequest.class));
    }

    @Test
    public void acceptLoanProposal_NotFound() {
        Long proposalId = 999L;
        when(loanProposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> loanService.acceptLoanProposal(proposalId));
    }

    @Test
    public void acceptLoanProposal_NotPending() {
        Long proposalId = 1L;
        LoanProposal proposal = new LoanProposal();
        proposal.setId(proposalId);
        proposal.setStatus(ProposalStatus.REJECTED);

        when(loanProposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));

        assertThrows(IllegalStateException.class, () -> loanService.acceptLoanProposal(proposalId));
    }
}
