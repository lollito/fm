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
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Player;
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
	@Autowired private CountryService countryService;
	
	public Game create(String gameName){
		Game game = new Game();
		game.setName(gameName);
		for(Country country : countryService.findAll()){
			League league = new League();
			league.setName(gameName + "_" + country.getName());
			league.setCountry(country);
			game.addLeague(league);
			List<Club> clubs = clubService.createClubs(game, 10);
			league.setClubs(clubs);
			league.setCurrentSeason(seasonService.create(game, LocalDate.of( 2017 , Month.AUGUST , 21 )));
		};
		
		game.setCurrentDate(LocalDate.of(2017, Month.AUGUST, 24));
		game = gameRepository.save(game);
		sessionBean.setGameId(game.getId());
		return game;
	}
	
	public Game create(String clubName, String gameName){
		Game game = new Game();
		game.setName(gameName);
		League league = new League();
		league.setName(gameName);
		game.addLeague(league);
		List<Club> clubs = clubService.createClubs(game, 9);
		clubs.add(clubService.createPlayerClub(clubName, game));
		league.setClubs(clubs);
		league.setCurrentSeason(seasonService.create(game, LocalDate.of( 2017 , Month.AUGUST , 21 )));
		game.setCurrentDate(LocalDate.of(2017, Month.AUGUST, 24));
		game = gameRepository.save(game);
		sessionBean.setGameId(game.getId());
		return game;
	}
	
	public GameResponse next(){
		GameResponse gameResponse = new GameResponse();
		Game game = sessionBean.getGame();
		List<Match> matches = matchRepository.findByRoundSeasonAndDateAndFinish(game.getLeagues().get(0).getCurrentSeason(), game.getCurrentDate().plusDays(1), Boolean.FALSE);
		if(matches.isEmpty()){
			incrementPlayersCondition(game);
		} else {
			simulationMatchService.simulate(matches);
			Match match =  matches.get(matches.size() -1);
			if(match.getLast()) {
				game.getLeagues().get(0).getCurrentSeason().setNextRoundNumber(match.getRound().getNumber() + 1);
				if(match.getRound().getLast()){
					game.getLeagues().get(0).addSeasonHistory(game.getLeagues().get(0).getCurrentSeason());
					game.getLeagues().get(0).setCurrentSeason(seasonService.create(game, game.getCurrentDate().plusDays(2)));
				}
			} 
		}
		game.addDay();
		gameResponse.setCurrentDate(game.getCurrentDate());
		gameResponse.setDisputatedMatch(matches);
		
		game = gameRepository.save(game);
		return gameResponse;
	}

	private void incrementPlayersCondition(Game game) {
		for(Club club : game.getLeagues().get(0).getClubs()) {
			for(Player player : club.getTeam().getPlayers()) {
				double increment = -((10 * player.getStamina())/99) + (1000/99);
	        	player.incrementCondition(increment);
			}
		}
	}
	
	public GameResponse load(){
		GameResponse gameResponse = new GameResponse();
		Game game = sessionBean.getGame();
		gameResponse.setCurrentDate(game.getCurrentDate());
		return gameResponse;
	}
	
	public GameResponse load(Long gameId){
		Game game = gameRepository.findById(gameId).get();
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
		gameRepository.deleteById(gameId);
	}
}
