package com.lollito.fm.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.repository.rest.GameRepository;

@Service
public class GameService {
	
	@Autowired GameRepository gameRepository;
	@Autowired SeasonService seasonService;
	@Autowired ClubService clubService;
	
	public void create(){
		Game game = new Game();
		game.setName("new");
		List<Club> clubs = clubService.createClubs(game);
		game.setClubs(clubs);
		game.setCurrentSeason(seasonService.create(clubs, game));
		game.setCurrentDate(LocalDate.of(2017, Month.AUGUST, 1));
		gameRepository.save(game);
	}
}
