package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
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
import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.ServerRepository;

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
    @InjectMocks
    private ServerService serverService;

    @Mock private MatchRepository matchRepository;
    @Mock private PlayerService playerService;

    // Mocks required for dependency injection but not used in this test path
    @Mock private ServerRepository serverRepository;
    @Mock private SeasonRepository seasonRepository;
    @Mock private SeasonService seasonService;
    @Mock private ClubService clubService;
    @Mock private LeagueService leagueService;
    @Mock private SimulationMatchService simulationMatchService;
    @Mock private CountryService countryService;
    @Mock private UserService userService;
    @Mock private MatchMapper matchMapper;

    @Test
    public void testNextUpdatesPlayersInBatch() {
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
        List<Club> clubs = new ArrayList<>();
        int clubCount = 5;
        for (int i = 0; i < clubCount; i++) {
            Club club = new Club();
            Team team = new Team();
            List<Player> players = new ArrayList<>();
            Player player = new Player();
            player.setStamina(80.0);
            players.add(player);
            team.setPlayers(players);
            club.setTeam(team);
            clubs.add(club);
        }
        league.setClubs(clubs);

        // Mock empty matches to trigger the update logic
        when(matchRepository.findByRoundSeasonAndDateBeforeAndFinish(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Execute
        serverService.next(league);

        // Verification (Fixed - verify 1 batch call)
        verify(simulationMatchService, never()).simulate(any(Match.class));
        verify(simulationMatchService, times(1)).simulate(anyList());
        // Verify
        // In the optimized implementation, saveAll is called once for all players.
        verify(playerService, times(1)).saveAll(anyList());
    }
}
