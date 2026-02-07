package com.lollito.fm.service;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.when;


import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.rest.ServerResponse;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.Player;
import com.lollito.fm.repository.rest.ServerRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.SeasonRepository;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private UserService userService;

    @Mock
    private CountryService countryService;

    @Mock
    private ClubService clubService;

    @Mock
    private SeasonService seasonService;

    @Mock
    private LeagueService leagueService;

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SimulationMatchService simulationMatchService;

    @Mock
    private PlayerService playerService;

    @Mock
    private SeasonRepository seasonRepository;

    @InjectMocks
    private ServerService serverService;

    @Test
    void testDeleteAll() {
        serverService.deleteAll();
        verify(serverRepository).deleteAll();
    }

    @Test
    void testCreate() {
        String serverName = "Test Server";
        User user = new User();
        user.setUsername("owner");

        when(userService.getLoggedUser()).thenReturn(user);

        Country country = new Country();
        country.setName("Test Country");
        when(countryService.findByCreateLeague(true)).thenReturn(Collections.singletonList(country));

        when(clubService.createClubs(any(Server.class), any(League.class), eq(10))).thenReturn(new ArrayList<>());

        when(seasonService.create(any(League.class), any(LocalDateTime.class))).thenReturn(new Season());

        when(serverRepository.save(any(Server.class))).thenAnswer(i -> i.getArguments()[0]);

        Server result = serverService.create(serverName);

        assertNotNull(result);
        assertEquals(serverName, result.getName());
        assertEquals(user, result.getOwner());
        assertNotNull(result.getCurrentDate());
        // Allow a small time difference
        assertTrue(result.getCurrentDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(result.getCurrentDate().isAfter(LocalDateTime.now().minusSeconds(10)));

        verify(countryService).findByCreateLeague(true);
        verify(clubService).createClubs(eq(result), any(League.class), eq(10));
        verify(seasonService).create(any(League.class), eq(result.getCurrentDate()));
        verify(serverRepository).save(result);
    }
    void testLoadById_Success() {
        Long serverId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Server server = new Server();
        server.setId(serverId);
        server.setCurrentDate(now);

        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        ServerResponse response = serverService.load(serverId);

        assertNotNull(response);
        assertEquals(now, response.getCurrentDate());
    }

    @Test
    void testLoadById_NotFound() {
        Long serverId = 1L;
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serverService.load(serverId));
    }

    @Test
    void testLoadCurrent_Success() {
        User user = new User();
        Server server = new Server();
        LocalDateTime now = LocalDateTime.now();
        server.setCurrentDate(now);
        user.setServer(server);

        when(userService.getLoggedUser()).thenReturn(user);

        ServerResponse response = serverService.load();

        assertNotNull(response);
        assertEquals(now, response.getCurrentDate());
    }

    @Test
    void testLoadCurrent_NoServer() {
        User user = new User();
        user.setServer(null);

        when(userService.getLoggedUser()).thenReturn(user);

        assertThrows(EntityNotFoundException.class, () -> serverService.load());
    }

    @Test
    void testNext_NoMatches() {
        // Setup
        League league = new League();
        Season season = new Season();
        league.setCurrentSeason(season);

        when(leagueRepository.findAllWithCurrentSeason()).thenReturn(Collections.singletonList(league));
        when(matchRepository.findByRoundSeasonInAndDateBeforeAndFinish(anyList(), any(LocalDateTime.class), eq(Boolean.FALSE)))
            .thenReturn(Collections.emptyList());

        Club club = new Club();
        Team team = new Team();
        team.setId(1L);
        club.setTeam(team);
        when(clubRepository.findAllByLeagueInWithTeam(anyList())).thenReturn(Collections.singletonList(club));

        Player player = new Player();
        player.setStamina(80.0);
        player.setCondition(100.0);
        when(playerRepository.findByTeamIdIn(anyList())).thenReturn(Collections.singletonList(player));

        // Execute
        serverService.next();

        // Verify
        verify(leagueRepository).findAllWithCurrentSeason();
        verify(matchRepository).findByRoundSeasonInAndDateBeforeAndFinish(anyList(), any(LocalDateTime.class), eq(Boolean.FALSE));
        verify(clubRepository).findAllByLeagueInWithTeam(anyList());
        verify(playerRepository).findByTeamIdIn(anyList());
        verify(playerService).updateSkills(anyList());
        verify(playerService).saveAll(anyList());
    }
}
