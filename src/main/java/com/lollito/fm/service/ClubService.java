package com.lollito.fm.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.utils.RandomUtils;

@Service
public class ClubService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired TeamService teamService;
	@Autowired NameService nameService;
	
	public void createPlayerClub(){
	}
	
	public List<Club> createClubs(Game game){
		int clubNumber = 20;
		
		List<Club> clubs = new ArrayList<>();
		
		for(int clubCreated = 0; clubCreated < clubNumber; clubCreated ++){
			Club club = new Club();
			club.setName(nameService.generateClubName());
			club.setGame(game);
			club.setFoundation(LocalDate.now());
			club.setTeam(teamService.createTeam());
			club.setStadium(new Stadium(club.getName() + " Stadium", RandomUtils.randomValue(15000, 40000)));
			clubs.add(club);
		}
		
		return clubs;
	}
	
}
