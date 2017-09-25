package com.lollito.fm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerPosition;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.SimulationMatch;
import com.lollito.fm.utils.RandomUtils;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("start run");
//		PlayerService ps = new PlayerService();
//		int teamNumber = 20;
//		
//		int gk = 3;
//		int cd = 4;
//		int wb = 4;
//		int mf = 4;
//		int wng = 4;
//		int fw = 4;
//		    
//		Map<String, Integer> ranking = new HashMap<>();
//		
//		List<Team> teams = new ArrayList<>();
//		
//		for(int teamCreated = 0; teamCreated < teamNumber; teamCreated ++){
//			Team team = new Team("Team" + teamCreated);
//			ranking.put(team.getName(), 0);
//			for (int i = 0; i < gk; i++) {
//				team.addPlayer(ps.createGk(new Player("Player GK", "Player GK" , Dates.generateRandomDate())));
//			}
//			for (int i = 0; i < cd; i++) {
//				team.addPlayer(ps.createCd(new Player("Player CD", "Player CD" , Dates.generateRandomDate())));
//			}
//			for (int i = 0; i < wb; i++) {
//				team.addPlayer(ps.createWb(new Player("Player WB", "Player WB" , Dates.generateRandomDate())));
//			}
//			for (int i = 0; i <mf; i++) {
//				team.addPlayer(ps.createMf(new Player("Player MF", "Player MF" , Dates.generateRandomDate())));
//			}
//			for (int i = 0; i < wng; i++) {
//				team.addPlayer(ps.createWng(new Player("Player WNG", "Player WNG" , Dates.generateRandomDate())));
//			}
//			for (int i = 0; i < fw; i++) {
//				team.addPlayer(ps.createFw(new Player("Player FW", "Player FW" , Dates.generateRandomDate())));
//			}
//			teams.add(team);
//		}
		
		//logger.info("teams: {}", teams);
//		for (Team team : teams) {
//			logger.info("team average : {}", team.getAverage()/20 + RandomUtils.randomValue(-1, 1));
//		}
//		Season season = createSeason(teams);
//		
//		for (Round round : season.getRounds()) {
//			logger.info("nuovo round");
//			for (Match match : round.getMatches()) {
//				SimulationMatch simulationMatch = new SimulationMatch();
//				simulationMatch.setHomeFormation(createFormation(match.getHome().getPlayers()));
//				simulationMatch.setAwayFormation(createFormation(match.getAway().getPlayers()));
//				simulationMatch.setMatch(match);
//				int[] score = playMatch(simulationMatch);
//				if(score[0] > score[1]){
//					ranking.put(match.getHome().getName(), ranking.get(match.getHome().getName()) +3);
//				} else if(score[0] < score[1]){
//					ranking.put(match.getAway().getName(), ranking.get(match.getAway().getName()) +3);
//				} else{
//					ranking.put(match.getHome().getName(), ranking.get(match.getHome().getName()) +1);
//					ranking.put(match.getAway().getName(), ranking.get(match.getAway().getName()) +1);
//				}
//			}
//		}
//		logger.info("ranking {}", ranking);
		
	}

	private List<Module> createModules() {
		List<Module> modules = new ArrayList<>();
		modules.add(new Module("4-4-2", 2, 2, 2, 2, 2));
		modules.add(new Module("4-3-3", 2, 2, 3, 2, 1));
		modules.add(new Module("4-3-3 offensive", 2, 2, 3, 0, 3));
		return modules;
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
		homePlayers.put(PlayerPosition.DEFENCE, getDefender(simulationMatch.getHomeFormation()));
		homePlayers.put(PlayerPosition.MIDFIELD, getMiedfileder(simulationMatch.getHomeFormation()));
		homePlayers.put(PlayerPosition.OFFENCE, getOffender(simulationMatch.getHomeFormation()));
		
		awayPlayers.put(PlayerPosition.DEFENCE, getDefender(simulationMatch.getAwayFormation()));
		awayPlayers.put(PlayerPosition.MIDFIELD, getMiedfileder(simulationMatch.getAwayFormation()));
		awayPlayers.put(PlayerPosition.OFFENCE, getOffender(simulationMatch.getAwayFormation()));
		
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
				if((getOffenceAverage(homePlayers.get(playerPosition)) - getDefenceAverage(awayPlayers.get(inversePosition.get(playerPosition)))) * rndm > 100){
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
				if((getOffenceAverage(awayPlayers.get(playerPosition)) - getDefenceAverage(homePlayers.get(inversePosition.get(playerPosition)))) * rndm > 100){
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
		
		logger.info("{} vs {}", homeScore, awayScore);
		return new int[]{homeScore, awayScore};
	}
	
	private List<Player> getDefender(Formation formation){
		List<Player> players = new ArrayList<>();
		players.add(formation.getGoalKeeper());
		players.addAll(formation.getCentralDefenders());
		players.addAll(formation.getWingBacks());
		players.addAll(formation.getMidfielders());
		return players;
	}
	
	private List<Player> getMiedfileder(Formation formation){
		List<Player> players = new ArrayList<>();
		players.addAll(formation.getCentralDefenders());
		players.addAll(formation.getWingBacks());
		players.addAll(formation.getMidfielders());
		players.addAll(formation.getWings());
		return players;
	}
	
	private List<Player> getOffender(Formation formation){
		List<Player> players = new ArrayList<>();
		players.addAll(formation.getMidfielders());
		players.addAll(formation.getWings());
		players.addAll(formation.getForwards());
		return players;
	}
	
	private Integer getOffenceAverage(List<Player> players){
		int tot = 0;
		for (Player player : players) {
			tot += player.getOffenceAverage();
		}
		return tot/players.size();
	}
	
	private Integer getDefenceAverage(List<Player> players){
		int tot = 0;
		for (Player player : players) {
			tot += player.getDefenceAverage();
		}
		return tot/players.size();
	}
	
	private Formation createFormation(List<Player> players){
		//logger.info("players {}", players.size());
		List<Player> playersCopy = new ArrayList<>();
		playersCopy.addAll(players);
		Module module = RandomUtils.randomValueFromList(createModules());
		Formation formation = new Formation();
		Player goalKeeper = getBestDefensivePlayer(playersCopy, PlayerRole.GOALKEEPER);
		formation.addPlayer(goalKeeper);
		playersCopy.remove(goalKeeper);
		for (int i = 0; i < module.getCd(); i++) {
			Player best = getBestDefensivePlayer(playersCopy, PlayerRole.CENTRALDEFENDER);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getWb(); i++) {
			Player best = getBestDefensivePlayer(playersCopy, PlayerRole.WINGBACK);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getMf(); i++) {
			Player best = getBestBalancedPlayer(playersCopy, PlayerRole.MIDFIELDER);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getWng(); i++) {
			Player best = getBestBalancedPlayer(playersCopy, PlayerRole.WING);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getFw(); i++) {
			Player best = getBestOffensivePlayer(playersCopy, PlayerRole.FORWARD);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		//logger.info("formation {}", formation);
		return formation;
	}
	
	private Player getBestDefensivePlayer(List<Player> players, PlayerRole playerRole){
		Player best = null;
		for (Player player : players) {
			if(player.getRole().getvalue() == playerRole.getvalue()){
				if(best == null){
					best = player;
				} else if(player.getDefenceAverage() > best.getDefenceAverage()){
					best = player;
				}
			}
		}
		return best;
	}
	
	private Player getBestOffensivePlayer(List<Player> players, PlayerRole playerRole){
		Player best = null;
		for (Player player : players) {
			if(player.getRole().getvalue() == playerRole.getvalue()){
				if(best == null){
					best = player;
				} else if(player.getOffenceAverage() > best.getOffenceAverage()){
					best = player;
				}
			}
		}
		return best;
	}
	
	private Player getBestBalancedPlayer(List<Player> players, PlayerRole playerRole){
		Player best = null;
		for (Player player : players) {
			if(player.getRole().getvalue() == playerRole.getvalue()){
				if(best == null){
					best = player;
				} else if(player.getDefenceAverage() > best.getDefenceAverage() && player.getOffenceAverage() > best.getOffenceAverage()){
					best = player;
				}
			}
		}
		return best;
	}
	
	public static void main(String[] args) {
		try {
			new DatabaseLoader().run("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
