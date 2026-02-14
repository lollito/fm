package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.RankingRepository;
import com.lollito.fm.repository.rest.SponsorRepository;
import com.lollito.fm.repository.rest.SponsorshipOfferRepository;

@ExtendWith(MockitoExtension.class)
public class SponsorshipServiceOptimizationTest {

    @InjectMocks
    private SponsorshipService sponsorshipService;

    @Mock private RankingRepository rankingRepository;
    @Mock private SeasonService seasonService;
    @Mock private ClubService clubService;
    @Mock private SponsorRepository sponsorRepository;
    @Mock private SponsorshipOfferRepository sponsorshipOfferRepository;

    @Test
    public void testGenerateSponsorshipOffers_CallsFindAll() {
        // Setup
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        club.setName("Test Club");
        // Ensure club has necessary fields to avoid NPEs
        Finance finance = new Finance();
        finance.setBalance(BigDecimal.ZERO);
        club.setFinance(finance);
        Stadium stadium = new Stadium();
        stadium.setCapacity(10000);
        club.setStadium(stadium);

        Season currentSeason = new Season();
        currentSeason.setId(100L);

        Ranking ranking = new Ranking();
        ranking.setClub(club);
        ranking.setSeason(currentSeason);
        ranking.setPoints(10);

        List<Ranking> allRankings = new ArrayList<>();
        allRankings.add(ranking);

        // Mocks
        when(clubService.findById(clubId)).thenReturn(club);
        when(seasonService.getCurrentSeason()).thenReturn(currentSeason);
        when(rankingRepository.findByClubAndSeason(club, currentSeason)).thenReturn(ranking);
        when(rankingRepository.findDistinctBySeasonOrderByPointsDesc(currentSeason)).thenReturn(allRankings);
        when(sponsorRepository.findAll()).thenReturn(new ArrayList<>()); // Just return empty sponsors
        when(sponsorshipOfferRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Execute
        sponsorshipService.generateSponsorshipOffers(clubId);

        // Verify
        verify(rankingRepository).findDistinctBySeasonOrderByPointsDesc(currentSeason);
        verify(rankingRepository, org.mockito.Mockito.never()).findAll();
    }
}
