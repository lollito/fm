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
import com.lollito.fm.model.Game;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.User;
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
	
	public Club createPlayerClub(String clubName, Game game){
		User user = userService.find();
		return createClub(clubName, game, user);
	}
	
	public List<Club> createClubs(Game game){
		//TODO config file
		int clubNumber = 3;
		
		List<Club> clubs = new ArrayList<>();
		
		for(int clubCreated = 0; clubCreated < clubNumber; clubCreated ++){
			Club club = createClub(null, game, null);
			clubs.add(club);
		}
		
		return clubs;
	}

	public Club load(){
		return clubRepository.findByGameAndUser(sessionBean.getGame(), userService.find());
	}
	
	private Club createClub(String clubName, Game game, User user) {
		Club club = new Club();
		club.setName(clubName != null ? clubName : nameService.generateClubName());
		club.setGame(game);
		club.setUser(user);
		club.setFoundation(LocalDate.now());
		club.setTeam(teamService.createTeam());
		club.setStadium(new Stadium(club.getName() + " Stadium", RandomUtils.randomValue(15000, 40000)));
		return club;
	}
	
}
