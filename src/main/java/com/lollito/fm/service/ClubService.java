package com.lollito.fm.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.bean.SessionBean;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class ClubService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired TeamService teamService;
	@Autowired NameService nameService;
	@Autowired UserService userService;
	@Autowired ClubRepository clubRepository;
	@Autowired SessionBean sessionBean;
	
	public List<Club> createClubs(Game game, League league, int clubNumber){
		List<Club> clubs = new ArrayList<>();
		for(int clubCreated = 0; clubCreated < clubNumber; clubCreated ++){
			Club club = createClub(null, game);
			club.setLeague(league);
			clubs.add(club);
		}
		
		return clubs;
	}

	public Club load(){
		return userService.find().getClub();
	}
	
	private Club createClub(String clubName, Game game) {
		Club club = new Club();
		club.setName(clubName != null ? clubName : nameService.generateClubName());
		club.setLeague(game.getLeagues().get(0));
		club.setFoundation(LocalDate.now());
		club.setTeam(teamService.createTeam());
		club.setStadium(new Stadium(club.getName() + " Stadium", RandomUtils.randomValue(15000, 40000)));
		return club;
	}

	public Long getCount() {
		return clubRepository.count();
	}
	
	//TODO exception free club not found
	public Club findTopByLeagueCountryAndUserIsNull(Country country) {
		return clubRepository.findTopByLeagueCountryAndUserIsNull(country).orElseThrow();
	}
}
