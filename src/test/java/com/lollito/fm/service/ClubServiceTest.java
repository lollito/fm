package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.exception.FreeClubNotFoundException;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Server;
import com.lollito.fm.repository.rest.ClubRepository;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private NameService nameService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ClubService clubService;

    @Test
    void findTopByLeagueCountryAndUserIsNull_ShouldThrowException_WhenClubNotFound() {
        Country country = new Country();
        when(clubRepository.findTopByLeagueCountryAndUserIsNull(country)).thenReturn(Optional.empty());

        assertThrows(FreeClubNotFoundException.class, () -> clubService.findTopByLeagueCountryAndUserIsNull(country));
    }

    @Test
    void findTopByLeagueServerAndLeagueCountryAndUserIsNull_ShouldThrowException_WhenClubNotFound() {
        Server server = new Server();
        Country country = new Country();
        when(clubRepository.findTopByLeagueServerAndLeagueCountryAndUserIsNull(server, country))
                .thenReturn(Optional.empty());

        assertThrows(FreeClubNotFoundException.class, () -> clubService.findTopByLeagueServerAndLeagueCountryAndUserIsNull(server, country));
    }

    @Test
    void findTopByLeagueCountry_ShouldThrowException_WhenClubNotFound() {
        Country country = new Country();
        when(clubRepository.findTopByLeagueCountry(country)).thenReturn(Optional.empty());

        assertThrows(FreeClubNotFoundException.class, () -> clubService.findTopByLeagueCountry(country));
    }
}
