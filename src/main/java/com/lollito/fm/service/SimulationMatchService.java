package com.lollito.fm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerPosition;
import com.lollito.fm.model.SimulationMatch;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.SimulationMatchRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class SimulationMatchService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired FormationService formationService;
	@Autowired PlayerService playerService;
	@Autowired SimulationMatchRepository simulationMatchRepository;
	@Autowired MatchRepository matchRepository;
	@Autowired ModuleRepository moduleRepository;
	@Autowired RankingService rankingService;
	@Autowired PlayerRepository playerRepository;
	
	public Match simulate(List<Match> matches){
		Match userMatch = null;
		for (Match match : matches) {
			if(match.getHome().getUser() == null && match.getAway().getUser() == null){
				simulate(match);
			} else {
				if((match.getHome().getUser() != null && match.getHome().getTeam().getFormation() != null) || (match.getAway().getUser() != null && match.getAway().getTeam().getFormation() != null)) {
					simulate(match);
				} else {
					userMatch =  match;
				}
				
			}
		}
		return userMatch;
	}
	
	public void simulate(Match match){
		SimulationMatch simulationMatch = new SimulationMatch();
		if(match.getHome().getUser() == null) {
			match.getHome().getTeam().setFormation(formationService.createFormation(match.getHome().getTeam().getPlayers(), match.getHome().getTeam().getFormation()));
		} else if(match.getHome().getTeam().getFormation() == null) {
			throw new RuntimeException("Salvare formazione");
		}
		
		if(match.getAway().getUser() == null) {
			match.getAway().getTeam().setFormation((formationService.createFormation(match.getAway().getTeam().getPlayers(), match.getAway().getTeam().getFormation())));
		} else if(match.getAway().getTeam().getFormation() == null) {
			throw new RuntimeException("Salvare formazione");
		}
		
		simulationMatch.setMatch(match);
		simulationMatchRepository.save(simulationMatch);
		playMatch(simulationMatch);
		match.setFinish(true);
//		matchRepository.save(simulationMatch.getMatch());
//		matchRepository.save(simulationMatch.getMatch());
		simulationMatchRepository.save(simulationMatch);
	}
	
	private int[] playMatch(SimulationMatch simulationMatch){
//		Let's say you have an "action" every 5 minutes of the game, so 90/5 = 18 actions. To make it more realistic you can choose random number like:
		Integer numberOfActions = RandomUtils.randomValue(10,20);
		logger.debug("numberOfActions {}", numberOfActions);
		
		Integer homeScore = 0;
		Integer awayScore = 0;
		
		int coin = RandomUtils.randomValue(0, 1);
		if (coin == 0){
			simulationMatch.getMatch().getHome().getTeam().getFormation().setHaveBall(true);
			//logger.info("home have ball");
		} else {
			simulationMatch.getMatch().getAway().getTeam().getFormation().setHaveBall(true);
			//logger.info("away have ball");
		}
		
		PlayerPosition playerPosition = PlayerPosition.MIDFIELD;
		Map<PlayerPosition, List<Player>> homePlayers = new HashMap<>();
		Map<PlayerPosition, List<Player>> awayPlayers = new HashMap<>();
		homePlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(simulationMatch.getMatch().getHome().getTeam().getFormation()));
		homePlayers.put(PlayerPosition.MIDFIELD, formationService.getMiedfileder(simulationMatch.getMatch().getHome().getTeam().getFormation()));
		homePlayers.put(PlayerPosition.OFFENCE, formationService.getOffender(simulationMatch.getMatch().getHome().getTeam().getFormation()));
		
		awayPlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(simulationMatch.getMatch().getAway().getTeam().getFormation()));
		awayPlayers.put(PlayerPosition.MIDFIELD, formationService.getMiedfileder(simulationMatch.getMatch().getAway().getTeam().getFormation()));
		awayPlayers.put(PlayerPosition.OFFENCE, formationService.getOffender(simulationMatch.getMatch().getAway().getTeam().getFormation()));
		
		Map<PlayerPosition, PlayerPosition> inversePosition = new HashMap<>();
		inversePosition.put(PlayerPosition.DEFENCE, PlayerPosition.OFFENCE);
		inversePosition.put(PlayerPosition.MIDFIELD, PlayerPosition.MIDFIELD);
		inversePosition.put(PlayerPosition.OFFENCE, PlayerPosition.DEFENCE);
		int luckHome = 20;
		int luckAway = 20;
		for (int i = 0; i <= numberOfActions; i++) {
			//logger.info("rndm {}", rndm);
			if(simulationMatch.getMatch().getHome().getTeam().getFormation().getHaveBall()){
				int rndm = RandomUtils.randomValue(0, luckHome);
				if(rndm > (luckHome/2)){
					luckHome-=RandomUtils.randomValue(0, 5);
					if(luckHome < 0){
						luckHome = 8;
					}
				} else{
					luckHome+=RandomUtils.randomValue(0, 5);
				}
				//logger.info("getOffenceAverage Home {}", getOffenceAverage(homePlayers.get(playerPosition)));
				//logger.info("getDefenceAverage Away {}", getDefenceAverage(awayPlayers.get(inversePosition.get(playerPosition))));
//				logger.info("homePlayers.get(playerPosition).size() {}", homePlayers.get(playerPosition).size());
//				logger.info("awayPlayers.get(inversePosition.get(playerPosition)).size() {}", awayPlayers.get(inversePosition.get(playerPosition)).size());
				if((playerService.getOffenceAverage(homePlayers.get(playerPosition)) + homePlayers.get(playerPosition).size() - playerService.getDefenceAverage(awayPlayers.get(inversePosition.get(playerPosition))) + awayPlayers.get(inversePosition.get(playerPosition)).size()) * rndm > 100){
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						//logger.info("home ball -> {}", playerPosition.getvalue());
					}else {
						homeScore++;
						playerPosition = PlayerPosition.MIDFIELD;
						simulationMatch.getMatch().getHome().getTeam().getFormation().setHaveBall(false);
						simulationMatch.getMatch().getAway().getTeam().getFormation().setHaveBall(true);
						logger.debug("home gol");
					}
				}else{
					logger.debug("home lost ball");
					simulationMatch.getMatch().getHome().getTeam().getFormation().setHaveBall(false);
					simulationMatch.getMatch().getAway().getTeam().getFormation().setHaveBall(true);
				}
			} else {
				int rndm = RandomUtils.randomValue(0, luckAway);
				if(rndm > (luckAway/2)){
					luckAway-=RandomUtils.randomValue(0, 5);
					if(luckAway < 0){
						luckAway = 8;
					}
				} else{
					luckAway+=RandomUtils.randomValue(0, 5);
				}
				//logger.info("getOffenceAverage Away {}", getOffenceAverage(awayPlayers.get(playerPosition)));
				//logger.info("getDefenceAverage Home {}", getDefenceAverage(homePlayers.get(inversePosition.get(playerPosition))));
//				logger.info("awayPlayers.get(playerPosition).size() {}", awayPlayers.get(playerPosition).size());
//				logger.info("homePlayers.get(inversePosition.get(playerPosition)).size() {}", homePlayers.get(inversePosition.get(playerPosition)).size());
				if((playerService.getOffenceAverage(awayPlayers.get(playerPosition)) + awayPlayers.get(playerPosition).size() - playerService.getDefenceAverage(homePlayers.get(inversePosition.get(playerPosition))) + homePlayers.get(inversePosition.get(playerPosition)).size()) * rndm > 100){
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						//logger.info("away ball -> {}", playerPosition.getvalue());
					}else {
						awayScore++;
						playerPosition = PlayerPosition.MIDFIELD;
						simulationMatch.getMatch().getHome().getTeam().getFormation().setHaveBall(true);
						simulationMatch.getMatch().getAway().getTeam().getFormation().setHaveBall(false);
						//logger.info("away gol");
					}
				}else{
					//logger.info("away lost ball");
					simulationMatch.getMatch().getHome().getTeam().getFormation().setHaveBall(true);
					simulationMatch.getMatch().getAway().getTeam().getFormation().setHaveBall(false);
				}
			}
			for(Player player : simulationMatch.getMatch().getHome().getTeam().getFormation().getPlayers()) {
	        	double d = -((10 * player.getStamina())/99) + (1000/99);
	        	player.decrementCondition(d);
	        }
			for(Player player : simulationMatch.getMatch().getAway().getTeam().getFormation().getPlayers()) {
	        	double d = -((10 * player.getStamina())/99) + (1000/99);
	        	player.decrementCondition(d);
	        }
		}
		simulationMatch.getMatch().setHomeScore(homeScore);
		simulationMatch.getMatch().setAwayScore(awayScore);
		
		rankingService.update(simulationMatch.getMatch());
		
		logger.info("{} vs {}", homeScore, awayScore);
		return new int[]{homeScore, awayScore};
	}
}
