package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
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
	@Autowired private LeagueService leagueService;
	@Autowired private SimulationMatchService simulationMatchService;
	@Autowired private CountryService countryService;
	@Autowired private PlayerService playerService;
	
	public Game create(String gameName){
		Game game = new Game();
		game.setName(gameName);
//		LocalDate gameStartDate = LocalDate.of(2020, Month.AUGUST, 21);
		LocalDateTime gameStartDate = LocalDateTime.now();
		for(Country country : countryService.findByCreateLeague(true)){
			League league = new League();
			league.setName(gameName + "_" + country.getName());
			league.setCountry(country);
			game.addLeague(league);
			List<Club> clubs = clubService.createClubs(game, league, 10);
			league.setClubs(clubs);
			league.setCurrentSeason(seasonService.create(league, gameStartDate));
		};
		
		game.setCurrentDate(gameStartDate);
		game = gameRepository.save(game);
//		sessionBean.setGameId(game.getId());
		return game;
	}
	
	public GameResponse next() {
		leagueService.findAll().forEach(league -> next(league));
		return  new GameResponse();
	}
	public GameResponse next(League league){
		GameResponse gameResponse = new GameResponse();
		List<Match> matches = matchRepository.findByRoundSeasonAndDateBeforeAndFinish(league.getCurrentSeason(), LocalDateTime.now(), Boolean.FALSE);
		if(matches.isEmpty()){
			incrementPlayersCondition(league);
			updatePlayerSkills(league);
		} else {
			for (Match match : matches) {
				if (match.getStatus() == MatchStatus.SCHEDULED) {
					simulationMatchService.simulate(match);
				}
			}
			Match match =  matches.get(matches.size() -1);
			if(match.getLast()) {
				league.getCurrentSeason().setNextRoundNumber(match.getRound().getNumber() + 1);
				if(match.getRound().getLast()){
					league.addSeasonHistory(league.getCurrentSeason());
					league.setCurrentSeason(seasonService.create(league, LocalDateTime.now().plusMinutes(10)));
				}
			} 
		}
		gameResponse.setDisputatedMatch(matches);
		
		leagueService.save(league);
		return gameResponse;
	}

	private void updatePlayerSkills(League league) {
		for(Club club : league.getClubs()) {
			playerService.updateSkills(club.getTeam().getPlayers());
		}
	}
	
	private void incrementPlayersCondition(League league) {
		for(Club club : league.getClubs()) {
			for(Player player : club.getTeam().getPlayers()) {
				double increment = -((10 * player.getStamina())/99) + (1000/99);
	        	player.incrementCondition(increment);
			}
		}
	}
	
	public GameResponse load(){
		GameResponse gameResponse = new GameResponse();
		//FIXME
//		Game game = sessionBean.getGame();
		Game game = new Game();
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
//			sessionBean.setGameId(gameId);
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

	public void deleteAll(){
		gameRepository.deleteAll();
	}
}
