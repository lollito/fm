package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.rest.PlayerCondition;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class PlayerService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired PlayerRepository playerRepository;
	
	public Player findOne(Long id) {
		return playerRepository.findById(id).get();
	}
	
	public List<PlayerCondition> findAllCondition() {
		return playerRepository.findAllBy(PageRequest.of(0, 20));
	}
	
	public Player createGk(Player player){
		player.setStamina(RandomUtils.randomValue(20D, 90D));
		player.setPlaymaking(RandomUtils.randomValue(1D, 50D));
		player.setScoring(RandomUtils.randomValue(1D, 20D));
		player.setWinger(RandomUtils.randomValue(1D, 20D));
		player.setGoalkeeping(RandomUtils.randomValue(45D, 99D));
		player.setPassing(RandomUtils.randomValue(1D, 20D));
		player.setDefending(RandomUtils.randomValue(30D, 80D));
		player.setSetPieces(RandomUtils.randomValue(40D, 99D));
		player.setRole(PlayerRole.GOALKEEPER);
		return player;
	}
	
	public Player createCd(Player player){
		player.setStamina(RandomUtils.randomValue(20D, 90D));
		player.setPlaymaking(RandomUtils.randomValue(20D, 70D));
		player.setScoring(RandomUtils.randomValue(20D, 50D));
		player.setWinger(RandomUtils.randomValue(1D, 20D));
		player.setGoalkeeping(RandomUtils.randomValue(1D, 20D));
		player.setPassing(RandomUtils.randomValue(20D, 70D));
		player.setDefending(RandomUtils.randomValue(40D, 99D));
		player.setSetPieces(RandomUtils.randomValue(20D, 80D));
		player.setRole(PlayerRole.DEFENDER);
		return player;
	}
	
	public Player createWb(Player player){
		player.setStamina(RandomUtils.randomValue(20D, 90D));
		player.setPlaymaking(RandomUtils.randomValue(30D, 70D));
		player.setScoring(RandomUtils.randomValue(20D, 60D));
		player.setWinger(RandomUtils.randomValue(30D, 60D));
		player.setGoalkeeping(RandomUtils.randomValue(1D, 20D));
		player.setPassing(RandomUtils.randomValue(30D, 80D));
		player.setDefending(RandomUtils.randomValue(40D, 80D));
		player.setSetPieces(RandomUtils.randomValue(20D, 99D));
		player.setRole(PlayerRole.WINGBACK);
		return player;
	}
	
	public Player createMf(Player player){
		player.setStamina(RandomUtils.randomValue(20D, 90D));
		player.setPlaymaking(RandomUtils.randomValue(40D, 99D));
		player.setScoring(RandomUtils.randomValue(30D, 70D));
		player.setWinger(RandomUtils.randomValue(20D, 50D));
		player.setGoalkeeping(RandomUtils.randomValue(1D, 20D));
		player.setPassing(RandomUtils.randomValue(30D, 80D));
		player.setDefending(RandomUtils.randomValue(35D, 80D));
		player.setSetPieces(RandomUtils.randomValue(40D, 99D));
		player.setRole(PlayerRole.MIDFIELDER);
		return player;
	}
	
	public Player createWng(Player player){
		player.setStamina(RandomUtils.randomValue(20D, 90D));
		player.setPlaymaking(RandomUtils.randomValue(40D, 80D));
		player.setScoring(RandomUtils.randomValue(30D, 80D));
		player.setWinger(RandomUtils.randomValue(40D, 99D));
		player.setGoalkeeping(RandomUtils.randomValue(1D, 20D));
		player.setPassing(RandomUtils.randomValue(40D, 99D));
		player.setDefending(RandomUtils.randomValue(20D, 60D));
		player.setSetPieces(RandomUtils.randomValue(40D, 99D));
		player.setRole(PlayerRole.WING);
		return player;
	}
	
	public Player createFw(Player player){
		player.setStamina(RandomUtils.randomValue(20D, 90D));
		player.setPlaymaking(RandomUtils.randomValue(20D, 30D));
		player.setScoring(RandomUtils.randomValue(50D, 99D));
		player.setWinger(RandomUtils.randomValue(20D, 60D));
		player.setGoalkeeping(RandomUtils.randomValue(1D, 20D));
		player.setPassing(RandomUtils.randomValue(30D, 60D));
		player.setDefending(RandomUtils.randomValue(10D, 50D));
		player.setSetPieces(RandomUtils.randomValue(40D, 99D));
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
	
	public void updateSkills(Player player) {
		Double update = 0.0;
		if(player.getAge() < 21) {
			update = RandomUtils.randomValue(0.0, 0.2);
		} else if(player.getAge() < 28) {
			update = RandomUtils.randomValue(0.0, 0.1);
		} else if(player.getAge() < 35) {
			update = RandomUtils.randomValue(-0.1, 0.1);
		} else if(player.getAge() < 41) {
			update = RandomUtils.randomValue(-0.2, 0.1);
		} else {
			throw new RuntimeException("Retired player");
		}
		player.updateSkills(update);
	}

	public void updateSkills(List<Player> players) {
		players.parallelStream().forEach(player -> updateSkills(player));
	}

	public void saveAll(List<Player> players) {
		playerRepository.saveAll(players);
	}
	
	public Player save(Player player) {
		return playerRepository.save(player);
	}
	
	public List<Player> findByOnSale(Boolean onSale){
		return playerRepository.findByOnSale(onSale);
	}

	public Player onSale(Long id) {
		Player player = findOne(id);
		player.setOnSale(Boolean.TRUE);
		return save(player);
	}

	public Player changeRole(Long id, Integer roleValue) {
		Player player = findOne(id);
		player.setRole(PlayerRole.valueOf(roleValue));
		return save(player);
	}
}
