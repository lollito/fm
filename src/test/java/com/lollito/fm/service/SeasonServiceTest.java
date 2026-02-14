package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.dto.SeasonAdvancementResult;
import com.lollito.fm.repository.rest.SeasonRepository;

@ExtendWith(MockitoExtension.class)
public class SeasonServiceTest {

    @InjectMocks
    private SeasonService seasonService;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private RankingService rankingService;

    @Mock
    private SimulationMatchService simulationMatchService;

    @Mock
    private LeagueService leagueService;

    @Mock
    private AchievementService achievementService;

    @Test
    public void testForceAdvanceSeason_NoCurrentSeason() {
        // Arrange
        when(seasonRepository.findAllByCurrentTrue()).thenReturn(Collections.emptyList());

        // Act
        SeasonAdvancementResult result = seasonService.forceAdvanceSeason(true, true, true);

        // Assert
        assertFalse(result.isSeasonAdvanced());
        assertEquals(0, result.getMatchesProcessed());
        assertEquals(0, result.getTransfersProcessed());

        verify(simulationMatchService, never()).simulate(any(Match.class));
        verify(leagueService, never()).save(any(League.class));
    }

    @Test
    public void testForceAdvanceSeason_WithCurrentSeason_SkipMatches() {
        // Arrange
        Season currentSeason = new Season();
        currentSeason.setId(1L);
        currentSeason.setCurrent(true);
        currentSeason.setStartYear(2023);
        currentSeason.setEndYear(2024);

        League league = new League();
        league.setId(1L);
        league.setCurrentSeason(currentSeason);
        currentSeason.setLeague(league);

        // Create 2 clubs (must be even number)
        Club club1 = new Club();
        club1.setId(1L);
        club1.setName("Club 1");

        Club club2 = new Club();
        club2.setId(2L);
        club2.setName("Club 2");

        List<Club> clubs = new ArrayList<>(Arrays.asList(club1, club2));
        league.setClubs(clubs);

        // Create Rounds and Matches
        Round round = new Round();
        Match match1 = new Match();
        match1.setId(101L);
        match1.setFinish(false); // Unfinished match

        Match match2 = new Match();
        match2.setId(102L);
        match2.setFinish(true); // Finished match

        round.setMatches(Arrays.asList(match1, match2));
        currentSeason.setRounds(Collections.singletonList(round));

        // Mock repository returning current season
        when(seasonRepository.findAllByCurrentTrue()).thenReturn(Collections.singletonList(currentSeason));

        // Act
        SeasonAdvancementResult result = seasonService.forceAdvanceSeason(true, true, true);

        // Assert
        assertTrue(result.isSeasonAdvanced());
        assertEquals(1, result.getMatchesProcessed()); // Only match1 was simulated

        // Verify match simulation
        verify(simulationMatchService, times(1)).simulate(match1);
        verify(simulationMatchService, never()).simulate(match2);

        // Verify season creation interactions
        // Inside create(), it calls findAllByCurrentTrue again, which we mocked.
        // It calls achievementService.checkSeasonAchievements
        verify(achievementService, times(1)).checkSeasonAchievements(currentSeason);
        // It calls seasonRepository.save to set current=false
        verify(seasonRepository, times(1)).save(currentSeason);
        // It calls rankingService.create
        verify(rankingService, times(1)).create(anyList(), any(Season.class));

        // Verify final league save
        verify(leagueService, times(1)).save(league);
    }

    @Test
    public void testForceAdvanceSeason_WithCurrentSeason_NoSkipMatches() {
        // Arrange
        Season currentSeason = new Season();
        currentSeason.setId(1L);
        currentSeason.setCurrent(true);
        currentSeason.setStartYear(2023);
        currentSeason.setEndYear(2024);

        League league = new League();
        league.setId(1L);
        league.setCurrentSeason(currentSeason);
        currentSeason.setLeague(league);

        // Create 2 clubs (must be even number)
        Club club1 = new Club();
        club1.setId(1L);
        club1.setName("Club 1");

        Club club2 = new Club();
        club2.setId(2L);
        club2.setName("Club 2");

        List<Club> clubs = new ArrayList<>(Arrays.asList(club1, club2));
        league.setClubs(clubs);

        // Create Rounds and Matches
        Round round = new Round();
        Match match1 = new Match();
        match1.setId(101L);
        match1.setFinish(false); // Unfinished match

        round.setMatches(Collections.singletonList(match1));
        currentSeason.setRounds(Collections.singletonList(round));

        // Mock repository returning current season
        when(seasonRepository.findAllByCurrentTrue()).thenReturn(Collections.singletonList(currentSeason));

        // Act
        // skipRemainingMatches = false
        SeasonAdvancementResult result = seasonService.forceAdvanceSeason(false, true, true);

        // Assert
        assertTrue(result.isSeasonAdvanced());
        assertEquals(0, result.getMatchesProcessed()); // No matches simulated

        // Verify NO match simulation
        verify(simulationMatchService, never()).simulate(any(Match.class));

        // Verify season creation interactions still happen
        verify(achievementService, times(1)).checkSeasonAchievements(currentSeason);
        verify(seasonRepository, times(1)).save(currentSeason);
        verify(rankingService, times(1)).create(anyList(), any(Season.class));
        verify(leagueService, times(1)).save(league);
    }
}
