package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.lollito.fm.model.LoanProposal;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.dto.CreateLoanProposalRequest;
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
    public void createLoanProposal_Success() {
        // Setup
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
        // Ensure player is not on loan
        player.setOriginalTeam(null);

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        Club targetClub = new Club();
        targetClub.setId(targetClubId);
        // Link target club to player's team
        targetClub.setTeam(playerTeam);
        playerTeam.setClub(targetClub);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(playerTeam)).thenReturn(Optional.of(targetClub));

        when(loanProposalRepository.save(any(LoanProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        LoanProposal result = loanService.createLoanProposal(request);

        // Verify
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
        // Setup
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
        // Both findByTeam and findByUnder18 return empty
        when(clubRepository.findByTeam(playerTeam)).thenReturn(Optional.empty());
        when(clubRepository.findByUnder18(playerTeam)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(EntityNotFoundException.class, () -> loanService.createLoanProposal(request));
    }

    @Test
    public void createLoanProposal_PlayerAlreadyOnLoan() {
        // Setup
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

        // Player is on loan because originalTeam is set
        Team originalTeam = new Team();
        originalTeam.setId(11L);
        player.setOriginalTeam(originalTeam);

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        // Target club exists
        Club targetClub = new Club();
        targetClub.setId(3L);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(currentTeam)).thenReturn(Optional.of(targetClub));

        // Execute & Verify
        assertThrows(IllegalStateException.class, () -> loanService.createLoanProposal(request), "Player is already on loan");
    }

    @Test
    public void createLoanProposal_LoanOwnPlayer() {
        // Setup
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
        player.setOriginalTeam(null); // Not on loan

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        // Target club is the same as proposing club
        Club targetClub = new Club();
        targetClub.setId(proposingClubId); // Same ID

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(playerTeam)).thenReturn(Optional.of(targetClub));

        // Execute & Verify
        assertThrows(IllegalStateException.class, () -> loanService.createLoanProposal(request), "Cannot loan own player");
    }
}
