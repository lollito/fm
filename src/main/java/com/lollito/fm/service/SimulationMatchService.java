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
import com.lollito.fm.repository.rest.SimulationMatchRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class SimulationMatchService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired FormationService formationService;
	@Autowired PlayerService playerService;
	@Autowired SimulationMatchRepository simulationMatchRepository;
	@Autowired MatchRepository matchRepository;
	@Autowired RankingService rankingService;
	
	public Match simulate(List<Match> matches){
		Match userMatch = null;
		for (Match match : matches) {
			if(match.getHome().getUser() == null && match.getAway().getUser() == null){
				simulate(match);
			} else{
				userMatch =  match;
			}
		}
		return userMatch;
	}
	
	public void simulate(Match match){
		SimulationMatch simulationMatch = new SimulationMatch();
		simulationMatch.setHomeFormation(formationService.createFormation(match.getHome().getTeam().getPlayers()));
		simulationMatch.setAwayFormation(formationService.createFormation(match.getAway().getTeam().getPlayers()));
		simulationMatch.setMatch(match);
		simulationMatchRepository.save(simulationMatch);
		int[] score = playMatch(simulationMatch);
		simulationMatch.setFinish(true);
//		matchRepository.save(simulationMatch.getMatch());
//		matchRepository.save(simulationMatch.getMatch());
		simulationMatchRepository.save(simulationMatch);
//		if(score[0] > score[1]){
//			ranking.put(match.getHome().getName(), ranking.get(match.getHome().getName()) +3);
//		} else if(score[0] < score[1]){
//			ranking.put(match.getAway().getName(), ranking.get(match.getAway().getName()) +3);
//		} else{
//			ranking.put(match.getHome().getName(), ranking.get(match.getHome().getName()) +1);
//			ranking.put(match.getAway().getName(), ranking.get(match.getAway().getName()) +1);
//		}
	}
	
	private int[] playMatch(SimulationMatch simulationMatch){
//		Let's say you have an "action" every 5 minutes of the game, so 90/5 = 18 actions. To make it more realistic you can choose random number like:
		Integer numberOfActions = RandomUtils.randomValue(10,20);
		//logger.info("numberOfActions {}", numberOfActions);
		
		Integer homeScore = 0;
		Integer awayScore = 0;
		
		int coin = RandomUtils.randomValue(0, 1);
		if (coin == 0){
			simulationMatch.getHomeFormation().setHaveBall(true);
			//logger.info("home have ball");
		} else {
			simulationMatch.getAwayFormation().setHaveBall(true);
			//logger.info("away have ball");
		}
		
		PlayerPosition playerPosition = PlayerPosition.MIDFIELD;
		Map<PlayerPosition, List<Player>> homePlayers = new HashMap<>();
		Map<PlayerPosition, List<Player>> awayPlayers = new HashMap<>();
		homePlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(simulationMatch.getHomeFormation()));
		homePlayers.put(PlayerPosition.MIDFIELD, formationService.getMiedfileder(simulationMatch.getHomeFormation()));
		homePlayers.put(PlayerPosition.OFFENCE, formationService.getOffender(simulationMatch.getHomeFormation()));
		
		awayPlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(simulationMatch.getAwayFormation()));
		awayPlayers.put(PlayerPosition.MIDFIELD, formationService.getMiedfileder(simulationMatch.getAwayFormation()));
		awayPlayers.put(PlayerPosition.OFFENCE, formationService.getOffender(simulationMatch.getAwayFormation()));
		
		Map<PlayerPosition, PlayerPosition> inversePosition = new HashMap<>();
		inversePosition.put(PlayerPosition.DEFENCE, PlayerPosition.OFFENCE);
		inversePosition.put(PlayerPosition.MIDFIELD, PlayerPosition.MIDFIELD);
		inversePosition.put(PlayerPosition.OFFENCE, PlayerPosition.DEFENCE);
		int luckHome = 30;
		int luckAway = 30;
		for (int i = 0; i <= numberOfActions; i++) {
			//logger.info("rndm {}", rndm);
			if(simulationMatch.getHomeFormation().getHaveBall()){
				int rndm = RandomUtils.randomValue(0, luckHome);
				if(rndm > (luckHome/2)){
					luckHome-=RandomUtils.randomValue(0, 5);
					if(luckHome < 0){
						luckHome = 10;
					}
				} else{
					luckHome+=RandomUtils.randomValue(0, 5);
				}
				//logger.info("getOffenceAverage Home {}", getOffenceAverage(homePlayers.get(playerPosition)));
				//logger.info("getDefenceAverage Away {}", getDefenceAverage(awayPlayers.get(inversePosition.get(playerPosition))));
				if((playerService.getOffenceAverage(homePlayers.get(playerPosition)) - playerService.getDefenceAverage(awayPlayers.get(inversePosition.get(playerPosition)))) * rndm > 100){
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						//logger.info("home ball -> {}", playerPosition.getvalue());
					}else {
						homeScore++;
						playerPosition = PlayerPosition.MIDFIELD;
						simulationMatch.getHomeFormation().setHaveBall(false);
						simulationMatch.getAwayFormation().setHaveBall(true);
						//logger.info("home gol");
					}
				}else{
					//logger.info("home lost ball");
					simulationMatch.getHomeFormation().setHaveBall(false);
					simulationMatch.getAwayFormation().setHaveBall(true);
				}
			} else {
				int rndm = RandomUtils.randomValue(0, luckAway);
				if(rndm > (luckAway/2)){
					luckAway-=RandomUtils.randomValue(0, 5);
					if(luckAway < 0){
						luckAway = 10;
					}
				} else{
					luckAway+=RandomUtils.randomValue(0, 5);
				}
				//logger.info("getOffenceAverage Away {}", getOffenceAverage(awayPlayers.get(playerPosition)));
				//logger.info("getDefenceAverage Home {}", getDefenceAverage(homePlayers.get(inversePosition.get(playerPosition))));
				if((playerService.getOffenceAverage(awayPlayers.get(playerPosition)) - playerService.getDefenceAverage(homePlayers.get(inversePosition.get(playerPosition)))) * rndm > 100){
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						//logger.info("away ball -> {}", playerPosition.getvalue());
					}else {
						awayScore++;
						playerPosition = PlayerPosition.MIDFIELD;
						simulationMatch.getHomeFormation().setHaveBall(true);
						simulationMatch.getAwayFormation().setHaveBall(false);
						//logger.info("away gol");
					}
				}else{
					//logger.info("away lost ball");
					simulationMatch.getHomeFormation().setHaveBall(true);
					simulationMatch.getAwayFormation().setHaveBall(false);
				}
			}
		}
		simulationMatch.getMatch().setHomeScore(homeScore);
		simulationMatch.getMatch().setAwayScore(awayScore);
		
		rankingService.update(simulationMatch.getMatch());
		
		logger.info("{} vs {}", homeScore, awayScore);
		return new int[]{homeScore, awayScore};
	}
}
