package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.exception.FreeClubNotFoundException;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.ClubRepository;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

	@Mock
	private TeamService teamService;

	@Mock
	private NameService nameService;

	@Mock
	private UserService userService;

	@Mock
	private ClubRepository clubRepository;

	@InjectMocks
	private ClubService clubService;

	@Test
	void testCreateClubs() {
		// Arrange
		Server server = new Server();
		League league = new League();
		int clubNumber = 5;
		String mockClubName = "Test Club";
		Team mockTeam = new Team();

		when(nameService.generateClubName()).thenReturn(mockClubName);
		when(teamService.createTeam()).thenReturn(mockTeam);

		// Act
		List<Club> result = clubService.createClubs(server, league, clubNumber);

		// Assert
		assertNotNull(result);
		assertEquals(clubNumber, result.size());

		for (Club club : result) {
			assertEquals(league, club.getLeague());
			assertEquals(mockClubName, club.getName());
			assertEquals(mockTeam, club.getTeam());
			assertNotNull(club.getFoundation());
			assertNotNull(club.getLogoURL());
			assertNotNull(club.getStadium());
			assertNotNull(club.getFinance());
		}

		verify(nameService, times(clubNumber)).generateClubName();
		verify(teamService, times(clubNumber)).createTeam();
	}

	@Test
	void testCreateClubsZero() {
		// Arrange
		Server server = new Server();
		League league = new League();
		int clubNumber = 0;

		// Act
		List<Club> result = clubService.createClubs(server, league, clubNumber);

		// Assert
		assertNotNull(result);
		assertEquals(0, result.size());

		verifyNoInteractions(nameService);
		verifyNoInteractions(teamService);
	}

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
