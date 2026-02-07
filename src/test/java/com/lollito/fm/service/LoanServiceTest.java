package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.lollito.fm.model.dto.CreateLoanProposalRequest;
import com.lollito.fm.model.dto.TransferOfferDTO;
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
    @Mock private FinancialService financialService;
    @Mock private PlayerService playerService;
    @Mock private PlayerHistoryService playerHistoryService;
    @Mock private LoanProposalRepository loanProposalRepository;
    @Mock private LoanPerformanceReviewRepository performanceReviewRepository;
    @Mock private ClubService clubService;
    @Mock private ClubRepository clubRepository;
    @Mock private SeasonService seasonService;
    @Mock private NewsService newsService;

    @Test
    public void activatePurchaseOption_Success() {
        Long loanId = 1L;
        BigDecimal optionPrice = new BigDecimal("5000000");

        Club parentClub = new Club();
        parentClub.setId(10L);
        parentClub.setName("Parent Club");

        Club loanClub = new Club();
        loanClub.setId(20L);
        loanClub.setName("Loan Club");
        Team loanTeam = new Team();
        loanTeam.setId(200L);
        loanClub.setTeam(loanTeam);

        Player player = new Player();
        player.setId(100L);
        player.setName("Player Name");
        Team originalTeam = new Team();
        originalTeam.setId(300L);
        player.setTeam(loanTeam);
        player.setOriginalTeam(originalTeam);

        LoanAgreement loan = LoanAgreement.builder()
                .id(loanId)
                .player(player)
                .parentClub(parentClub)
                .loanClub(loanClub)
                .hasOptionToBuy(true)
                .optionToBuyPrice(optionPrice)
                .status(LoanStatus.ACTIVE)
                .build();

        when(loanAgreementRepository.findById(loanId)).thenReturn(Optional.of(loan));

        TransferOfferDTO result = loanService.activatePurchaseOption(loanId);

        verify(financialService).processTransaction(eq(loanClub.getId()), argThat(request ->
            request.getType() == TransactionType.EXPENSE &&
            request.getCategory() == TransactionCategory.TRANSFER_FEES &&
            request.getAmount().equals(optionPrice)
        ));

        verify(financialService).processTransaction(eq(parentClub.getId()), argThat(request ->
            request.getType() == TransactionType.INCOME &&
            request.getCategory() == TransactionCategory.TRANSFER_INCOME &&
            request.getAmount().equals(optionPrice)
        ));

        assertEquals(loanTeam, player.getTeam());
        assertEquals(null, player.getOriginalTeam());
        verify(playerService).save(player);

        verify(playerHistoryService).recordTransfer(eq(player), eq(parentClub), eq(loanClub), eq(optionPrice), eq(TransferType.PURCHASE));

        assertEquals(LoanStatus.COMPLETED, loan.getStatus());
        verify(loanAgreementRepository).save(loan);

        assertNotNull(result);
        assertEquals(player.getId(), result.getPlayerId());
        assertEquals(loanClub.getId(), result.getBuyingClubId());
        assertEquals(parentClub.getId(), result.getSellingClubId());
        assertEquals(optionPrice, result.getOfferAmount());
        assertEquals("ACCEPTED", result.getStatus());
    }

    @Test
    public void activatePurchaseOption_LoanNotFound() {
        Long loanId = 1L;
        when(loanAgreementRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> loanService.activatePurchaseOption(loanId));
    }

    @Test
    public void activatePurchaseOption_NoOptionToBuy() {
        Long loanId = 1L;
        LoanAgreement loan = LoanAgreement.builder()
                .id(loanId)
                .hasOptionToBuy(false)
                .build();

        when(loanAgreementRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThrows(IllegalStateException.class, () -> loanService.activatePurchaseOption(loanId));
    }

    @Test
    public void createLoanProposal_Success() {
        Long playerId = 1L;
        Long proposingClubId = 2L;
        Long targetClubId = 3L;

        CreateLoanProposalRequest request = new CreateLoanProposalRequest();
        request.setPlayerId(playerId);
        request.setProposingClubId(proposingClubId);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusMonths(6));
        request.setLoanFee(new BigDecimal("100000"));
        request.setSalaryShare(0.5);
        request.setRecallClause(true);
        request.setOptionToBuy(true);
        request.setOptionPrice(new BigDecimal("2000000"));
        request.setMessage("Please give us this player");

        Player player = new Player();
        player.setId(playerId);
        Team playerTeam = new Team();
        playerTeam.setId(10L);
        player.setTeam(playerTeam);
        player.setOriginalTeam(null);

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        Club targetClub = new Club();
        targetClub.setId(targetClubId);
        targetClub.setTeam(playerTeam);
        playerTeam.setClub(targetClub);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(playerTeam)).thenReturn(Optional.of(targetClub));

        when(loanProposalRepository.save(any(LoanProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanProposal result = loanService.createLoanProposal(request);

        assertNotNull(result);
        assertEquals(player, result.getPlayer());
        assertEquals(proposingClub, result.getProposingClub());
        assertEquals(targetClub, result.getTargetClub());
        assertEquals(request.getStartDate(), result.getProposedStartDate());
        assertEquals(request.getEndDate(), result.getProposedEndDate());
        assertEquals(request.getLoanFee(), result.getProposedLoanFee());
        assertEquals(request.getSalaryShare(), result.getProposedSalaryShare());
        assertEquals(request.getRecallClause(), result.getProposedRecallClause());
        assertEquals(request.getOptionToBuy(), result.getProposedOptionToBuy());
        assertEquals(request.getOptionPrice(), result.getProposedOptionPrice());
        assertEquals(request.getMessage(), result.getProposalMessage());

        verify(loanProposalRepository).save(any(LoanProposal.class));
    }

    @Test
    public void createLoanProposal_TargetClubNotFound() {
        Long playerId = 1L;
        Long proposingClubId = 2L;

        CreateLoanProposalRequest request = new CreateLoanProposalRequest();
        request.setPlayerId(playerId);
        request.setProposingClubId(proposingClubId);

        Player player = new Player();
        player.setId(playerId);
        Team playerTeam = new Team();
        playerTeam.setId(10L);
        player.setTeam(playerTeam);

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(playerTeam)).thenReturn(Optional.empty());
        when(clubRepository.findByUnder18(playerTeam)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> loanService.createLoanProposal(request));
    }

    @Test
    public void createLoanProposal_PlayerAlreadyOnLoan() {
        Long playerId = 1L;
        Long proposingClubId = 2L;

        CreateLoanProposalRequest request = new CreateLoanProposalRequest();
        request.setPlayerId(playerId);
        request.setProposingClubId(proposingClubId);

        Player player = new Player();
        player.setId(playerId);
        Team currentTeam = new Team();
        currentTeam.setId(10L);
        player.setTeam(currentTeam);

        Team originalTeam = new Team();
        originalTeam.setId(11L);
        player.setOriginalTeam(originalTeam);

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        // Even if target club found, it should fail
        Club targetClub = new Club();
        targetClub.setId(3L);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(currentTeam)).thenReturn(Optional.of(targetClub));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            loanService.createLoanProposal(request);
        });
        assertEquals("Player is already on loan", exception.getMessage());
    }

    @Test
    public void createLoanProposal_LoanOwnPlayer() {
        Long playerId = 1L;
        Long proposingClubId = 2L;

        CreateLoanProposalRequest request = new CreateLoanProposalRequest();
        request.setPlayerId(playerId);
        request.setProposingClubId(proposingClubId);

        Player player = new Player();
        player.setId(playerId);
        Team playerTeam = new Team();
        playerTeam.setId(10L);
        player.setTeam(playerTeam);
        player.setOriginalTeam(null);

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        Club targetClub = new Club();
        targetClub.setId(proposingClubId);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(playerTeam)).thenReturn(Optional.of(targetClub));

        assertThrows(IllegalStateException.class, () -> loanService.createLoanProposal(request), "Cannot loan own player");
    }

    @Test
    public void acceptLoanProposal_Success() {
        Long proposalId = 1L;
        Long playerId = 100L;
        Long parentClubId = 200L;
        Long loanClubId = 300L;

        Player player = new Player();
        player.setId(playerId);

        Team currentTeam = new Team();
        currentTeam.setId(10L);
        player.setTeam(currentTeam);

        Team originalTeam = new Team();
        originalTeam.setId(11L);
        player.setOriginalTeam(originalTeam);
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

        assertNotNull(agreement);
        assertEquals(LoanStatus.ACTIVE, agreement.getStatus());
        assertEquals(player, agreement.getPlayer());
        assertEquals(parentClub, agreement.getParentClub());
        assertEquals(loanClub, agreement.getLoanClub());
        assertEquals(proposal.getProposedLoanFee(), agreement.getLoanFee());

        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
        assertNotNull(proposal.getResponseDate());
        verify(loanProposalRepository).save(proposal);

        assertEquals(loanTeam, player.getTeam());
        assertEquals(parentTeam, player.getOriginalTeam());
        verify(playerService).save(player);
        verify(playerHistoryService).recordTransfer(eq(player), eq(parentClub), eq(loanClub), eq(BigDecimal.ZERO), eq(TransferType.LOAN));

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
