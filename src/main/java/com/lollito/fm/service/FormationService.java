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
import com.lollito.fm.model.Team;

@Service
public class FormationService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ModuleService moduleService;
	@Autowired private PlayerService playerService;
	@Autowired private ClubService clubService;
	@Autowired private MentalityService mentalityService;
	
	public Formation createPlayerFormation() {
		Team team = clubService.load().getTeam();
        return createFormation(team.getPlayers(), team.getFormation());
	}
	
	public Formation createFormation(List<Player> players, Formation formation){
		//logger.info("players {}", players.size());
		List<Player> playersCopy = new ArrayList<>();
		playersCopy.addAll(players);
		Module module = moduleService.randomModule();
		if (formation == null) {
			formation = new Formation();
		}
		formation.setModule(module);
		formation.setMentality(mentalityService.random());
		formation.setPlayers(new ArrayList<>());
		Player goalKeeper = playerService.getBestDefensivePlayer(playersCopy, PlayerRole.GOALKEEPER);
		formation.addPlayer(goalKeeper);
		playersCopy.remove(goalKeeper);
		for (int i = 0; i < module.getCd(); i++) {
			Player best = playerService.getBestDefensivePlayer(playersCopy, PlayerRole.DEFENDER);
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
		if(formation.getMentality().getvalue() < 2){
			players.addAll(formation.getMidfielders());
		}
		if(formation.getMentality().getvalue() < 1){
			players.addAll(formation.getWings());
		}
		return players;
	}
	
	public List<Player> getMiedfileder(Formation formation){
		List<Player> players = new ArrayList<>();
		players.addAll(formation.getCentralDefenders());
		players.addAll(formation.getWingBacks());
		players.addAll(formation.getMidfielders());
		if(formation.getMentality().getvalue() < 2){
			players.addAll(formation.getWings());
		}
		if(formation.getMentality().getvalue() < 1){
			players.addAll(formation.getForwards());
		}
		return players;
	}
	
	public List<Player> getOffender(Formation formation){
		List<Player> players = new ArrayList<>();
		players.addAll(formation.getWings());
		players.addAll(formation.getForwards());
		if(formation.getMentality().getvalue() > 0){
			players.addAll(formation.getMidfielders());
		}
		if(formation.getMentality().getvalue() > 1){
			players.addAll(formation.getWingBacks());
		}
		return players;
	}
	
	public void validate(Formation formation) {
		if(formation.getPlayers().size() != 11) {
			throw new RuntimeException("formation validation error");
		}
		if(!formation.getModule().getCd().equals(formation.getCentralDefenders().size())) {
			throw new RuntimeException("formation validation error");
		}
		if(!formation.getModule().getWb().equals(formation.getWingBacks().size())) {
			throw new RuntimeException("formation validation error");
		}
		if(!formation.getModule().getMf().equals(formation.getMidfielders().size())) {
			throw new RuntimeException("formation validation error");
		}
		if(!formation.getModule().getWng().equals(formation.getWings().size())) {
			throw new RuntimeException("formation validation error");
		}
		if(!formation.getModule().getFw().equals(formation.getForwards().size())) {
			throw new RuntimeException("formation validation error");
		}
	}
}
