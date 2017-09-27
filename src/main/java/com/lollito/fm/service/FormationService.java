package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.utils.RandomUtils;

@Service
public class FormationService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired ModuleService moduleService;
	@Autowired PlayerService playerService;
	
	public Formation createFormation(List<Player> players){
		//logger.info("players {}", players.size());
		List<Player> playersCopy = new ArrayList<>();
		playersCopy.addAll(players);
		Module module = moduleService.randomModule();
		Formation formation = new Formation();
		formation.setModule(module);
		Player goalKeeper = playerService.getBestDefensivePlayer(playersCopy, PlayerRole.GOALKEEPER);
		formation.addPlayer(goalKeeper);
		playersCopy.remove(goalKeeper);
		for (int i = 0; i < module.getCd(); i++) {
			Player best = playerService.getBestDefensivePlayer(playersCopy, PlayerRole.CENTRALDEFENDER);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getWb(); i++) {
			Player best = playerService.getBestDefensivePlayer(playersCopy, PlayerRole.WINGBACK);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getMf(); i++) {
			Player best = playerService.getBestBalancedPlayer(playersCopy, PlayerRole.MIDFIELDER);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getWng(); i++) {
			Player best = playerService.getBestBalancedPlayer(playersCopy, PlayerRole.WING);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		for (int i = 0; i < module.getFw(); i++) {
			Player best = playerService.getBestOffensivePlayer(playersCopy, PlayerRole.FORWARD);
			formation.addPlayer(best);
			playersCopy.remove(best);
		}
		//logger.info("formation {}", formation);
		return formation;
	}
	
	public List<Player> getDefender(Formation formation){
		List<Player> players = new ArrayList<>();
		players.add(formation.getGoalKeeper());
		players.addAll(formation.getCentralDefenders());
		players.addAll(formation.getWingBacks());
		players.addAll(formation.getMidfielders());
		return players;
	}
	
	public List<Player> getMiedfileder(Formation formation){
		List<Player> players = new ArrayList<>();
		players.addAll(formation.getCentralDefenders());
		players.addAll(formation.getWingBacks());
		players.addAll(formation.getMidfielders());
		players.addAll(formation.getWings());
		return players;
	}
	
	public List<Player> getOffender(Formation formation){
		List<Player> players = new ArrayList<>();
		players.addAll(formation.getMidfielders());
		players.addAll(formation.getWings());
		players.addAll(formation.getForwards());
		return players;
	}
}
