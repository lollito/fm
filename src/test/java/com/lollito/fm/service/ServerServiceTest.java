package com.lollito.fm.service;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.repository.rest.ServerRepository;

import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Season;

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
}
