package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;

@ExtendWith(MockitoExtension.class)
public class ServerServiceOptimizationTest {

    @Mock MatchRepository matchRepository;
    @Mock SeasonRepository seasonRepository;
    @Mock SeasonService seasonService;
    @Mock ClubService clubService;
    @Mock LeagueService leagueService;
    @Mock SimulationMatchService simulationMatchService;
    @Mock CountryService countryService;
    @Mock PlayerService playerService;
    @Mock UserService userService;
    @Mock MatchMapper matchMapper;

    @InjectMocks
    ServerService serverService;

    @Test
    void testNextBatchProcessing() {
        // Setup
        League league = new League();
        Season season = new Season();
        league.setCurrentSeason(season);

        List<Match> matches = new ArrayList<>();
        int matchCount = 5;
        for(int i=0; i<matchCount; i++) {
            Match m = new Match();
            m.setId((long)i);
            m.setStatus(MatchStatus.SCHEDULED);
            m.setRound(new Round());
            m.setLast(false);
            matches.add(m);
        }

        when(matchRepository.findByRoundSeasonAndDateBeforeAndFinish(eq(season), any(), eq(Boolean.FALSE)))
            .thenReturn(matches);

        when(matchMapper.toDto(any(Match.class))).thenReturn(new MatchDTO());

        // Execute
        serverService.next(league);

        // Verification (Fixed - verify 1 batch call)
        verify(simulationMatchService, never()).simulate(any(Match.class));
        verify(simulationMatchService, times(1)).simulate(anyList());
    }
}
