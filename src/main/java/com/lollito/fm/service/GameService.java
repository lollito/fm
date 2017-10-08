package com.lollito.fm.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.bean.SessionBean;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.rest.GameResponse;
import com.lollito.fm.repository.rest.GameRepository;
import com.lollito.fm.repository.rest.MatchRepository;

@Service
public class GameService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private GameRepository gameRepository;
	@Autowired private MatchRepository matchRepository;
	@Autowired private SeasonService seasonService;
	@Autowired private ClubService clubService;
	@Autowired private SessionBean sessionBean;
	@Autowired private SimulationMatchService simulationMatchService;
	
	public Game create(String clubName, String gameName){
		Game game = new Game();
		game.setName(gameName);
		List<Club> clubs = clubService.createClubs(game);
		clubs.add(clubService.createPlayerClub(clubName, game));
		game.setClubs(clubs);
		game.setCurrentSeason(seasonService.create(clubs, game));
		game.setCurrentDate(LocalDate.of(2017, Month.AUGUST, 24));
		game = gameRepository.save(game);
		sessionBean.setGameId(game.getId());
		return game;
	}
	
	public GameResponse next(){
		GameResponse gameResponse = new GameResponse();
		Game game = sessionBean.getGame();
		game.addDay();
		List<Match> matches = matchRepository.findByGameAndDate(game, game.getCurrentDate());
		logger.info("matches {}", matches);
		gameResponse.setCurrentMatch(simulationMatchService.simulate(matches));
		gameResponse.setCurrentDate(game.getCurrentDate());
		gameResponse.setDisputatedMatch(matches);
		game = gameRepository.save(game);
		return gameResponse;
	}
	
	public GameResponse load(){
		GameResponse gameResponse = new GameResponse();
		Game game = sessionBean.getGame();
		gameResponse.setCurrentDate(game.getCurrentDate());
		return gameResponse;
	}
	
	public GameResponse load(Long gameId){
		Game game = gameRepository.findOne(gameId);
		GameResponse gameResponse = new GameResponse();
		if (game == null){
			//TODO error
			logger.error("error - game is null");
		} else {
			sessionBean.setGameId(gameId);
			gameResponse.setCurrentDate(game.getCurrentDate());
		}
		return gameResponse;
	}
	
	public List<Game> findAll(){
		return gameRepository.findAll();
	}
	
	public void delete(Long gameId){
		//TODO security
		gameRepository.delete(gameId);
	}
}
