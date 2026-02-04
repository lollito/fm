package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.service.errors.FreeClubNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class ClubServiceTest {

	@Mock
	private ClubRepository clubRepository;

	@InjectMocks
	private ClubService clubService;

	@Test(expected = FreeClubNotFoundException.class)
	public void findTopByLeagueCountryAndUserIsNull_ShouldThrowException_WhenNoClubFound() {
		when(clubRepository.findTopByLeagueCountryAndUserIsNull(any(Country.class))).thenReturn(Optional.empty());
		clubService.findTopByLeagueCountryAndUserIsNull(new Country());
	}

	@Test
	public void findTopByLeagueCountryAndUserIsNull_ShouldReturnClub_WhenClubFound() {
		Club club = new Club();
		when(clubRepository.findTopByLeagueCountryAndUserIsNull(any(Country.class))).thenReturn(Optional.of(club));
		Club result = clubService.findTopByLeagueCountryAndUserIsNull(new Country());
		assertNotNull(result);
	}
}
