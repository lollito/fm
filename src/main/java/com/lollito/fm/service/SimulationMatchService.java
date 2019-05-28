package com.lollito.fm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerPosition;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class SimulationMatchService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired FormationService formationService;
	@Autowired PlayerService playerService;
	@Autowired MatchRepository matchRepository;
	@Autowired ModuleRepository moduleRepository;
	@Autowired RankingService rankingService;
	@Autowired PlayerRepository playerRepository;
	
	public void simulate(List<Match> matches){
		matches.forEach(match -> simulate(match));
	}
	
	public void simulate(Match match){
		//if(match.getHome().getTeam().getFormation() == null) {
			match.getHome().getTeam().setFormation(formationService.createFormation(match.getHome().getTeam().getPlayers(), match.getHome().getTeam().getFormation()));
		//}
		
		//if(match.getAway().getTeam().getFormation() == null) {
			match.getAway().getTeam().setFormation((formationService.createFormation(match.getAway().getTeam().getPlayers(), match.getAway().getTeam().getFormation())));
//		}
		
//		simulationMatchRepository.save(simulationMatch);
		playMatch(match);
		match.setFinish(true);
//		matchRepository.save(simulationMatch.getMatch());
		matchRepository.save(match);
//		simulationMatchRepository.save(simulationMatch);
	}
	
	private int[] playMatch(Match match){
//		Let's say you have an "action" every 5 minutes of the game, so 90/5 = 18 actions. To make it more realistic you can choose random number like:
		Integer numberOfActions = RandomUtils.randomValue(10,20);
		logger.debug("numberOfActions {}", numberOfActions);
		
		Integer homeScore = 0;
		Integer awayScore = 0;
		
		int coin = RandomUtils.randomValue(0, 1);
		Formation homeFormation = match.getHome().getTeam().getFormation();
		Formation awayFormation = match.getAway().getTeam().getFormation();
		if (coin == 0){
			homeFormation.setHaveBall(true);
			//logger.info("home have ball");
		} else {
			awayFormation.setHaveBall(true);
			//logger.info("away have ball");
		}
		
		PlayerPosition playerPosition = PlayerPosition.MIDFIELD;
		Map<PlayerPosition, List<Player>> homePlayers = new HashMap<>();
		Map<PlayerPosition, List<Player>> awayPlayers = new HashMap<>();
		homePlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(homeFormation));
		homePlayers.put(PlayerPosition.MIDFIELD, formationService.getMiedfileder(homeFormation));
		homePlayers.put(PlayerPosition.OFFENCE, formationService.getOffender(homeFormation));
		
		awayPlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(awayFormation));
		awayPlayers.put(PlayerPosition.MIDFIELD, formationService.getMiedfileder(awayFormation));
		awayPlayers.put(PlayerPosition.OFFENCE, formationService.getOffender(awayFormation));
		
		Map<PlayerPosition, PlayerPosition> inversePosition = new HashMap<>();
		inversePosition.put(PlayerPosition.DEFENCE, PlayerPosition.OFFENCE);
		inversePosition.put(PlayerPosition.MIDFIELD, PlayerPosition.MIDFIELD);
		inversePosition.put(PlayerPosition.OFFENCE, PlayerPosition.DEFENCE);
		int luckHome = 20;
		int luckAway = 20;
		for (int i = 0; i <= numberOfActions; i++) {
			//logger.info("rndm {}", rndm);
			if(homeFormation.getHaveBall()){
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
						homeFormation.setHaveBall(false);
						awayFormation.setHaveBall(true);
						logger.debug("home gol");
					}
				}else{
					logger.debug("home lost ball");
					homeFormation.setHaveBall(false);
					awayFormation.setHaveBall(true);
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
						homeFormation.setHaveBall(true);
						awayFormation.setHaveBall(false);
						//logger.info("away gol");
					}
				}else{
					//logger.info("away lost ball");
					homeFormation.setHaveBall(true);
					awayFormation.setHaveBall(false);
				}
			}
			for(Player player : homeFormation.getPlayers()) {
	        	double d = 5 - (5 * player.getStamina()/100 );
	        	player.decrementCondition(d);
	        }
			for(Player player : awayFormation.getPlayers()) {
				double d = 5 - (5 * player.getStamina()/100 );
	        	player.decrementCondition(d);
	        }
		}
		match.setHomeScore(homeScore);
		match.setAwayScore(awayScore);
		
		rankingService.update(match);
		
		logger.info("{} vs {}", homeScore, awayScore);
		return new int[]{homeScore, awayScore};
	}
}
