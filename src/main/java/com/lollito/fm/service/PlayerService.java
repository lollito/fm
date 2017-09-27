package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class PlayerService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public Player createGk(Player player){
		player.setStamina(RandomUtils.randomValue(1, 99));
		player.setPlaymaking(RandomUtils.randomValue(1, 50));
		player.setScoring(RandomUtils.randomValue(1, 20));
		player.setWinger(RandomUtils.randomValue(1, 20));
		player.setGoalkeeping(RandomUtils.randomValue(45, 99));
		player.setPassing(RandomUtils.randomValue(1, 20));
		player.setDefending(RandomUtils.randomValue(30, 80));
		player.setSetPieces(RandomUtils.randomValue(40, 99));
		player.setRole(PlayerRole.GOALKEEPER);
		return player;
	}
	
	public Player createCd(Player player){
		player.setStamina(RandomUtils.randomValue(1, 99));
		player.setPlaymaking(RandomUtils.randomValue(20, 70));
		player.setScoring(RandomUtils.randomValue(20, 50));
		player.setWinger(RandomUtils.randomValue(1, 20));
		player.setGoalkeeping(RandomUtils.randomValue(1, 20));
		player.setPassing(RandomUtils.randomValue(20, 70));
		player.setDefending(RandomUtils.randomValue(40, 99));
		player.setSetPieces(RandomUtils.randomValue(20, 80));
		player.setRole(PlayerRole.CENTRALDEFENDER);
		return player;
	}
	
	public Player createWb(Player player){
		player.setStamina(RandomUtils.randomValue(1, 99));
		player.setPlaymaking(RandomUtils.randomValue(30, 70));
		player.setScoring(RandomUtils.randomValue(20, 60));
		player.setWinger(RandomUtils.randomValue(30, 60));
		player.setGoalkeeping(RandomUtils.randomValue(1, 20));
		player.setPassing(RandomUtils.randomValue(30, 80));
		player.setDefending(RandomUtils.randomValue(40, 80));
		player.setSetPieces(RandomUtils.randomValue(20, 99));
		player.setRole(PlayerRole.WINGBACK);
		return player;
	}
	
	public Player createMf(Player player){
		player.setStamina(RandomUtils.randomValue(1, 99));
		player.setPlaymaking(RandomUtils.randomValue(40, 99));
		player.setScoring(RandomUtils.randomValue(30, 70));
		player.setWinger(RandomUtils.randomValue(20, 50));
		player.setGoalkeeping(RandomUtils.randomValue(1, 20));
		player.setPassing(RandomUtils.randomValue(30, 80));
		player.setDefending(RandomUtils.randomValue(35, 80));
		player.setSetPieces(RandomUtils.randomValue(40, 99));
		player.setRole(PlayerRole.MIDFIELDER);
		return player;
	}
	
	public Player createWng(Player player){
		player.setStamina(RandomUtils.randomValue(1, 99));
		player.setPlaymaking(RandomUtils.randomValue(40, 80));
		player.setScoring(RandomUtils.randomValue(30, 80));
		player.setWinger(RandomUtils.randomValue(40, 99));
		player.setGoalkeeping(RandomUtils.randomValue(1, 20));
		player.setPassing(RandomUtils.randomValue(40, 99));
		player.setDefending(RandomUtils.randomValue(20, 60));
		player.setSetPieces(RandomUtils.randomValue(40, 99));
		player.setRole(PlayerRole.WING);
		return player;
	}
	
	public Player createFw(Player player){
		player.setStamina(RandomUtils.randomValue(1, 99));
		player.setPlaymaking(RandomUtils.randomValue(20, 30));
		player.setScoring(RandomUtils.randomValue(50, 99));
		player.setWinger(RandomUtils.randomValue(20, 60));
		player.setGoalkeeping(RandomUtils.randomValue(1, 20));
		player.setPassing(RandomUtils.randomValue(30, 60));
		player.setDefending(RandomUtils.randomValue(10, 50));
		player.setSetPieces(RandomUtils.randomValue(40, 99));
		player.setRole(PlayerRole.FORWARD);
		return player;
	}
	
	public Player getBestDefensivePlayer(List<Player> players, PlayerRole playerRole){
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
	
	public Player getBestOffensivePlayer(List<Player> players, PlayerRole playerRole){
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
	
	public Player getBestBalancedPlayer(List<Player> players, PlayerRole playerRole){
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
	
	public Integer getOffenceAverage(List<Player> players){
		int tot = 0;
		for (Player player : players) {
			tot += player.getOffenceAverage();
		}
		return tot/players.size();
	}
	
	public Integer getDefenceAverage(List<Player> players){
		int tot = 0;
		for (Player player : players) {
			tot += player.getDefenceAverage();
		}
		return tot/players.size();
	}
}
