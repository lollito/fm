package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.lollito.fm.exception.FreeClubNotFoundException;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class ClubService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired TeamService teamService;
	@Autowired NameService nameService;
	@Lazy @Autowired UserService userService;
	@Autowired ClubRepository clubRepository;
	
	public List<Club> createClubs(Server server, League league, int clubNumber){
		List<Club> clubs = new ArrayList<>();
		for(int clubCreated = 0; clubCreated < clubNumber; clubCreated ++){
			Club club = createClub(null, server);
			club.setLeague(league);
			clubs.add(club);
		}
		return clubs;
	}

	private Club createClub(String clubName, Server server) {
		Club club = new Club();
		club.setName(clubName != null ? clubName : nameService.generateClubName());
		club.setFoundation(LocalDate.now());
		club.setTeam(teamService.createTeam());
		club.setLogoURL("https://picsum.photos/35?random=" + RandomUtils.randomValue(1, 2));
		club.setStadium(new Stadium(club.getName() + " Stadium"));
		club.setFinance(new Finance(new BigDecimal(RandomUtils.randomValue(500000, 1500000))));
		return club;
	}

	public Long getCount() {
		return clubRepository.count();
	}
	
	public Club findTopByLeagueCountryAndUserIsNull(Country country) {
		return clubRepository.findTopByLeagueCountryAndUserIsNull(country).orElseThrow(FreeClubNotFoundException::new);
	}

	public Club findTopByLeagueServerAndLeagueCountryAndUserIsNull(Server server, Country country) {
		return clubRepository.findTopByLeagueServerAndLeagueCountryAndUserIsNull(server, country).orElseThrow(FreeClubNotFoundException::new);
	}
	
	public Club findTopByLeagueCountry(Country country) {
		return clubRepository.findTopByLeagueCountry(country).orElseThrow(FreeClubNotFoundException::new);
	}

	public Club save(Club club) {
		return clubRepository.save(club);
	}

	public Club findById(Long id) {
		return clubRepository.findById(id).orElseThrow(() -> new RuntimeException("Club not found"));
	}

	public List<Club> findAll() {
		return clubRepository.findAll();
	}
}
