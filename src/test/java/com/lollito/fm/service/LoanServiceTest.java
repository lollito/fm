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
    public void createLoanProposal_ShouldThrowException_WhenPlayerAlreadyOnLoan() {
        // Arrange
        Long playerId = 1L;
        Long proposingClubId = 2L;

        Player player = new Player();
        player.setId(playerId);

        Team currentTeam = new Team();
        currentTeam.setId(10L);
        player.setTeam(currentTeam);

        Team originalTeam = new Team();
        originalTeam.setId(11L);
        player.setOriginalTeam(originalTeam); // Player is already on loan

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        Club targetClub = new Club();
        targetClub.setId(3L);

        CreateLoanProposalRequest request = new CreateLoanProposalRequest();
        request.setPlayerId(playerId);
        request.setProposingClubId(proposingClubId);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(currentTeam)).thenReturn(Optional.of(targetClub));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            loanService.createLoanProposal(request);
        });

        assertEquals("Player is already on loan", exception.getMessage());
    }

    @Test
    public void createLoanProposal_Success() {
        // Arrange
        Long playerId = 1L;
        Long proposingClubId = 2L;

        Player player = new Player();
        player.setId(playerId);

        Team currentTeam = new Team();
        currentTeam.setId(10L);
        player.setTeam(currentTeam);
        player.setOriginalTeam(null); // Player is NOT on loan

        Club proposingClub = new Club();
        proposingClub.setId(proposingClubId);

        Club targetClub = new Club();
        targetClub.setId(3L);

        CreateLoanProposalRequest request = new CreateLoanProposalRequest();
        request.setPlayerId(playerId);
        request.setProposingClubId(proposingClubId);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(6));
        request.setLoanFee(BigDecimal.valueOf(1000));
        request.setSalaryShare(0.5);

        when(playerService.findOne(playerId)).thenReturn(player);
        when(clubService.findById(proposingClubId)).thenReturn(proposingClub);
        when(clubRepository.findByTeam(currentTeam)).thenReturn(Optional.of(targetClub));
        when(loanProposalRepository.save(any(LoanProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoanProposal result = loanService.createLoanProposal(request);

        // Assert
        assertNotNull(result);
        assertEquals(player, result.getPlayer());
        assertEquals(proposingClub, result.getProposingClub());
        assertEquals(targetClub, result.getTargetClub());

        verify(loanProposalRepository).save(any(LoanProposal.class));
    }
}
