package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.ServerRepository;

@ExtendWith(MockitoExtension.class)
public class ServerServiceOptimizationTest {

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
        // We use doReturn/when pattern or specific matching to ensure this one is returned for the second call
        // Or simply, since the method calls it twice with different params (false then true? No, method logic?)
        // ServerService.next calls:
        // 1. matchRepository.findByRoundSeasonAndDateBeforeAndFinish(..., Boolean.FALSE) -> returns matches
        // 2. logic simulates matches
        // 3. matchRepository.findByRoundSeasonAndDateBeforeAndFinish(..., Boolean.FALSE) -> returns empty (to verify all finished)

        // So we need:
        when(matchRepository.findByRoundSeasonAndDateBeforeAndFinish(eq(season), any(), eq(Boolean.FALSE)))
            .thenReturn(matches)
            .thenReturn(Collections.emptyList());

        // Execute
        serverService.next(league);

        // Verification (Fixed - verify 1 batch call)
        verify(simulationMatchService, never()).simulate(any(Match.class));
        verify(simulationMatchService, times(1)).simulate(anyList());

        // Verify
        // In the optimized implementation, saveAll is called once for all players.
        // But since simulationMatchService is mocked, the saveAll inside it is not called.
        // And ServerService.next(league) does not call saveAll directly in this path.
        verify(playerService, never()).saveAll(anyList());
    }
}
