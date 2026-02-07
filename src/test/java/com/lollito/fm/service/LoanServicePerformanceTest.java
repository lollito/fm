package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.Season;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.LoanAgreementRepository;
import com.lollito.fm.repository.rest.LoanPerformanceReviewRepository;
import com.lollito.fm.repository.rest.LoanProposalRepository;

@ExtendWith(MockitoExtension.class)
public class LoanServicePerformanceTest {

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

    @InjectMocks
    private LoanService loanService;

    @Test
    public void testProcessMonthlyLoanReviews_PerformanceOptimized() {
        // Setup
        int loanCount = 100;
        List<LoanAgreement> loans = new ArrayList<>();
        Season season = new Season();
        season.setId(1L);

        when(seasonService.getCurrentSeason()).thenReturn(season);

        Map<Long, PlayerSeasonStats> statsMap = new HashMap<>();

        for (int i = 0; i < loanCount; i++) {
            Player player = new Player();
            player.setId((long) i);

            LoanAgreement loan = LoanAgreement.builder()
                    .id((long) i)
                    .player(player)
                    .status(LoanStatus.ACTIVE)
                    .build();
            loans.add(loan);

            statsMap.put(player.getId(), new PlayerSeasonStats());
        }

        when(loanAgreementRepository.findWithPlayerByStatus(LoanStatus.ACTIVE)).thenReturn(loans);
        when(playerHistoryService.getSeasonStatsForPlayers(any(), eq(season))).thenReturn(statsMap);

        // Execute
        loanService.processMonthlyLoanReviews();

        // Verify Optimized Behavior

        // 1. Verify findWithPlayerByStatus is called instead of findByStatus
        verify(loanAgreementRepository, times(1)).findWithPlayerByStatus(LoanStatus.ACTIVE);
        verify(loanAgreementRepository, never()).findByStatus(LoanStatus.ACTIVE);

        // 2. Verify bulk fetch for stats is called once
        verify(playerHistoryService, times(1)).getSeasonStatsForPlayers(any(), eq(season));

        // 3. Verify N+1 is gone: getPlayerSeasonStats should NOT be called
        verify(playerHistoryService, never()).getPlayerSeasonStats(any(Long.class), any(Long.class));

        // 4. Verify loop execution: save is still called N times for reviews and loan updates
        verify(performanceReviewRepository, times(loanCount)).save(any());
        verify(loanAgreementRepository, times(loanCount)).save(any(LoanAgreement.class));
    }
}
