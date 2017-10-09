package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.TeamRepository;
import com.lollito.fm.utils.Dates;
import com.lollito.fm.utils.RandomUtils;

@Service
public class TeamService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired PlayerService playerService;
	@Autowired TeamRepository teamRepository;
	@Autowired NameService nameService;
	
	public Team createTeam(){
		
		int gk = 3;
		int cd = 4;
		int wb = 4;
		int mf = 4;
		int wng = 4;
		int fw = 4;
		
		List<String> names = new ArrayList<>();
		List<String> surnames = new ArrayList<>();
		
		names = nameService.getNames();
		surnames = nameService.getSurnames();
		
		Team team = new Team();
		for (int i = 0; i < gk; i++) {
			team.addPlayer(playerService.createGk(new Player(RandomUtils.randomValueFromList(names), RandomUtils.randomValueFromList(surnames) , Dates.generateRandomDate())));
		}
		for (int i = 0; i < cd; i++) {
			team.addPlayer(playerService.createCd(new Player(RandomUtils.randomValueFromList(names), RandomUtils.randomValueFromList(surnames) , Dates.generateRandomDate())));
		}
		for (int i = 0; i < wb; i++) {
			team.addPlayer(playerService.createWb(new Player(RandomUtils.randomValueFromList(names), RandomUtils.randomValueFromList(surnames) , Dates.generateRandomDate())));
		}
		for (int i = 0; i <mf; i++) {
			team.addPlayer(playerService.createMf(new Player(RandomUtils.randomValueFromList(names), RandomUtils.randomValueFromList(surnames) , Dates.generateRandomDate())));
		}
		for (int i = 0; i < wng; i++) {
			team.addPlayer(playerService.createWng(new Player(RandomUtils.randomValueFromList(names), RandomUtils.randomValueFromList(surnames) , Dates.generateRandomDate())));
		}
		for (int i = 0; i < fw; i++) {
			team.addPlayer(playerService.createFw(new Player(RandomUtils.randomValueFromList(names), RandomUtils.randomValueFromList(surnames) , Dates.generateRandomDate())));
		}
		return team;
		
	}
	
	public void save(List<Team> teams){
		teamRepository.save(teams);
	}
}
