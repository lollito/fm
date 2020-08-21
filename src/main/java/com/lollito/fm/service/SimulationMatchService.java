package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Event;
import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerPosition;
import com.lollito.fm.model.Stats;
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
		Integer stadiumCapacity = match.getHome().getStadium().getCapacity();
		match.setSpectators(RandomUtils.randomValue(stadiumCapacity/3, stadiumCapacity));
		//if(match.getHome().getTeam().getFormation() == null) {
			match.getHome().getTeam().setFormation(formationService.createFormation(match.getHome().getTeam().getPlayers(), match.getHome().getTeam().getFormation()));
		//}
		
		//if(match.getAway().getTeam().getFormation() == null) {
			match.getAway().getTeam().setFormation((formationService.createFormation(match.getAway().getTeam().getPlayers(), match.getAway().getTeam().getFormation())));
//		}
		
		playMatch(match);
		match.setFinish(true);
		matchRepository.save(match);
	}
	
	private int[] playMatch(Match match){
//		Let's say you have an "action" every 5 minutes of the game, so 90/5 = 18 actions. To make it more realistic you can choose random number like:
		Integer numberOfActions = RandomUtils.randomValue(12,22);
		logger.debug("numberOfActions {}", numberOfActions);
		
		Integer homeScore = 0;
		Integer awayScore = 0;
		
		int coin = RandomUtils.randomValue(0, 1);
		Formation homeFormation = match.getHome().getTeam().getFormation();
		Formation awayFormation = match.getAway().getTeam().getFormation();
		List<EventHistory> events = new ArrayList<>();
		Stats stats = new Stats();
		List<Player> cautionedPlayers = new ArrayList<>();
		if (coin == 0){
			homeFormation.setHaveBall(true);
			events.add(new EventHistory(String.format(Event.HAVE_BALL.getMessage(), match.getHome().getName()) , 0));
			//logger.info("home have ball");
		} else {
			awayFormation.setHaveBall(true);
			events.add(new EventHistory(String.format(Event.HAVE_BALL.getMessage(), match.getAway().getName()) , 0));
			//logger.info("away have ball");
		}
		
		PlayerPosition playerPosition = PlayerPosition.MIDFIELD;
		EnumMap<PlayerPosition, List<Player>> homePlayers = new EnumMap<>(PlayerPosition.class);
		EnumMap<PlayerPosition, List<Player>> awayPlayers = new EnumMap<>(PlayerPosition.class);
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
		int homePosession = 0;
		for (int actionNumber = 1; actionNumber <= numberOfActions; actionNumber++) {
			int minute = (90 * actionNumber) / numberOfActions;
			homePosession = homeFormation.getHaveBall() ? homePosession + 1 : homePosession;
			if(homeFormation.getHaveBall()){
				int luck = RandomUtils.randomValue(0, luckHome);
				if(luck > (luckHome/2)){
					luckHome-=RandomUtils.randomValue(0, 5);
					if(luckHome < 0){
						luckHome = 10;
					}
				} else{
					luckHome+=RandomUtils.randomValue(0, 5);
				}
				logger.debug("luck home {}", luck);
				int averageDiff = (playerService.getOffenceAverage(homePlayers.get(playerPosition)) + homePlayers.get(playerPosition).size()) - (playerService.getDefenceAverage(awayPlayers.get(inversePosition.get(playerPosition))) + awayPlayers.get(inversePosition.get(playerPosition)).size());
				logger.debug("home average diff {}", averageDiff);
				if((averageDiff > 0 && RandomUtils.randomPercentage(60 + averageDiff + luck)) || (averageDiff <= 0 && RandomUtils.randomPercentage(40 + averageDiff + luck))) {
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						logger.info("home ball -> {}", playerPosition.getvalue());
					}else {
							      
						
						Player scorer = RandomUtils.randomValueFromList(homePlayers.get(playerPosition));
						
						Integer goalKeeping = awayFormation.getGoalKeeper().getGoalkeepingAverage();
						
						if(scorer.getScoringAverage()  >= goalKeeping) {
							Integer diff = scorer.getScoringAverage() - goalKeeping;
							logger.debug("diff > {}", diff);
							if(RandomUtils.randomPercentage(60 + (diff / 2))) {
								homeScore++;
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(false);
								awayFormation.setHaveBall(true);
								stats.addHomeShot();
								stats.addHomeOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(), scorer.getSurname()) , minute));
								logger.info("home gol");
							} else {
								stats.addHomeShot();
								stats.addHomeOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute));
								logger.info("home corner");
							}
						} else {
							Integer diff = goalKeeping - scorer.getScoringAverage();
							logger.debug("diff < {}", diff);
							if(RandomUtils.randomPercentage(60 + (diff / 2))) {
								homeFormation.setHaveBall(false);
								awayFormation.setHaveBall(true);
								playerPosition = inversePosition.get(playerPosition);
								stats.addHomeShot();
								events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute));
								logger.info("shot missed");
							} else {
								homeScore++;
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(false);
								awayFormation.setHaveBall(true);
								stats.addHomeShot();
								stats.addHomeOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(), scorer.getSurname()) , minute));
								logger.info("home gol");
							}
						}
						
					}
				}else{
					if((averageDiff > 0 && RandomUtils.randomPercentage(40)) || averageDiff <= 0 && RandomUtils.randomPercentage(20)) {
						Player badPlayer = RandomUtils.randomValueFromList(awayPlayers.get(playerPosition));
						stats.addAwayFoul();
						events.add(new EventHistory(String.format(Event.COMMITS_FAUL.getMessage(), badPlayer.getName()) , minute));
						if(RandomUtils.randomPercentage(100 - badPlayer.getDefending())) {
							stats.addAwayYellowCard();
							if(cautionedPlayers.contains(badPlayer)) {
								for (PlayerPosition pps : PlayerPosition.values()) {
									awayPlayers.get(pps).remove(badPlayer);
								}
								events.add(new EventHistory(String.format(Event.RED_CARD.getMessage(), badPlayer.getName()) , minute));
							} else {
								cautionedPlayers.add(badPlayer);
								events.add(new EventHistory(String.format(Event.YELLOW_CARD.getMessage(), badPlayer.getName()) , minute));
							}
							
						}
						logger.info("away commits faul {}" , playerPosition);
						if(playerPosition.getvalue() == PlayerPosition.values().length -1 ) {
							Player scorer = homePlayers.get(playerPosition).stream().max(Comparator.comparing(Player::getPiecesAverage)).get();
							
							Integer goalKeeping = awayFormation.getGoalKeeper().getGoalkeepingAverage();
							
							if(scorer.getPiecesAverage()  >= goalKeeping) {
								Integer diff = scorer.getScoringAverage() - goalKeeping;
								logger.debug("diff > {}", diff);
								if(RandomUtils.randomPercentage(40 + (diff / 2))) {
									homeScore++;
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									stats.addHomeShot();
									stats.addHomeOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute));
									logger.info("home gol free kick");
								} else {
									stats.addHomeShot();
									stats.addHomeOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute));
									logger.info("home corner");
								}
							} else {
								Integer diff = goalKeeping - scorer.getScoringAverage();
								logger.debug("diff < {}", diff);
								if(RandomUtils.randomPercentage(70 + (diff / 2))) {
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									playerPosition = inversePosition.get(playerPosition);
									stats.addHomeShot();
									events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute));
									logger.info("shot missed");
								} else {
									homeScore++;
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									stats.addHomeShot();
									stats.addHomeOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute));
									logger.info("home gol free kick");
								}
							}
						}
					} else {
						logger.info("home lost ball {}" , playerPosition);
						playerPosition = inversePosition.get(playerPosition);
						homeFormation.setHaveBall(false);
						awayFormation.setHaveBall(true);
					}
					
				}
			} else {
				int luck = RandomUtils.randomValue(0, luckAway);
				if(luck > (luckAway/2)){
					luckAway-=RandomUtils.randomValue(0, 5);
					if(luckAway < 0){
						luckAway = 8;
					}
				} else{
					luckAway+=RandomUtils.randomValue(0, 5);
				}
				logger.debug("luck away {}", luck);
				int averageDiff = (playerService.getOffenceAverage(awayPlayers.get(playerPosition)) + awayPlayers.get(playerPosition).size()) - (playerService.getDefenceAverage(homePlayers.get(inversePosition.get(playerPosition))) + homePlayers.get(inversePosition.get(playerPosition)).size());
				logger.debug("away averageDiff {}", averageDiff);
				if((averageDiff > 0 && RandomUtils.randomPercentage(60 + averageDiff + luck)) || (averageDiff <= 0 && RandomUtils.randomPercentage(40 + averageDiff + luck))) {
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						logger.info("away ball -> {}", playerPosition.getvalue());
					}else {
						Player scorer = RandomUtils.randomValueFromList(awayPlayers.get(playerPosition));
						
						Integer goalKeeping = homeFormation.getGoalKeeper().getGoalkeepingAverage();
						
						if(scorer.getScoringAverage()  >= goalKeeping) {
							Integer diff = scorer.getScoringAverage() - goalKeeping;
							logger.debug("diff > {}", diff);
							if(RandomUtils.randomPercentage(60 + (diff / 2))) {
								awayScore++;
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(true);
								awayFormation.setHaveBall(false);
								stats.addAwayShot();
								stats.addAwayOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(),scorer.getSurname()) , minute));
								logger.info("away gol");
							} else {
								stats.addAwayShot();
								stats.addAwayOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute));
								logger.info("home corner");
							}
						} else {
							Integer diff = goalKeeping - scorer.getScoringAverage();
							logger.debug("diff < {}", diff);
							if(RandomUtils.randomPercentage(60 + (diff / 2))) {
								homeFormation.setHaveBall(true);
								awayFormation.setHaveBall(false);
								playerPosition = inversePosition.get(playerPosition);
								stats.addAwayShot();
								events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute));
								logger.info("shot missed");
							} else {
								awayScore++;
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(true);
								awayFormation.setHaveBall(false);
								stats.addAwayShot();
								stats.addAwayOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(),scorer.getSurname()) , minute));
								logger.info("away gol");
							}
						}
						
					}
				}else{
					if((averageDiff > 0 && RandomUtils.randomPercentage(40)) || averageDiff <= 0 && RandomUtils.randomPercentage(20)) {
						Player badPlayer = RandomUtils.randomValueFromList(homePlayers.get(playerPosition));
						stats.addHomeFoul();
						events.add(new EventHistory(String.format(Event.COMMITS_FAUL.getMessage(), badPlayer.getName()) , minute));
						if(RandomUtils.randomPercentage(100 - badPlayer.getDefending())) {
							stats.addHomeYellowCard();
							if(cautionedPlayers.contains(badPlayer)) {
								for (PlayerPosition pps : PlayerPosition.values()) {
									homePlayers.get(pps).remove(badPlayer);
								}
								events.add(new EventHistory(String.format(Event.RED_CARD.getMessage(), badPlayer.getName()) , minute));
							} else {
								cautionedPlayers.add(badPlayer);
								events.add(new EventHistory(String.format(Event.YELLOW_CARD.getMessage(), badPlayer.getName()) , minute));
							}
						}
						logger.info("home commits faul {}" , playerPosition);
						if(playerPosition.getvalue() == PlayerPosition.values().length -1 ) {
							Player scorer = awayPlayers.get(playerPosition).stream().max(Comparator.comparing(Player::getPiecesAverage)).get();
							
							Integer goalKeeping = homeFormation.getGoalKeeper().getGoalkeepingAverage();
							
							if(scorer.getPiecesAverage()  >= goalKeeping) {
								Integer diff = scorer.getScoringAverage() - goalKeeping;
								logger.debug("diff > {}", diff);
								if(RandomUtils.randomPercentage(40 + (diff / 2))) {
									homeScore++;
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									stats.addAwayShot();
									stats.addAwayOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute));
									logger.info("home gol free kick");
								} else {
									stats.addAwayShot();
									stats.addAwayOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute));
									logger.info("home corner");
								}
							} else {
								Integer diff = goalKeeping - scorer.getScoringAverage();
								logger.debug("diff < {}", diff);
								if(RandomUtils.randomPercentage(70 + (diff / 2))) {
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									playerPosition = inversePosition.get(playerPosition);
									stats.addAwayShot();
									events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute));
									logger.info("shot missed");
								} else {
									homeScore++;
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(true);
									awayFormation.setHaveBall(false);
									stats.addAwayShot();
									stats.addAwayOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute));
									logger.info("home gol free kick");
								}
							}
						}
					} else {
						logger.info("away lost ball {}" , playerPosition);
						playerPosition = inversePosition.get(playerPosition);
						homeFormation.setHaveBall(true);
						awayFormation.setHaveBall(false);
					}
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
		List<Player> players = Stream.concat(homeFormation.getPlayers().stream(), awayFormation.getPlayers().stream())
                .collect(Collectors.toList());
		
		playerService.saveAll(players);
		int homePosessionPerc = (homePosession * 100) / numberOfActions;
		stats.setHomePossession(homePosessionPerc);
		stats.setAwayPossession(100 - homePosessionPerc);
		
		logger.info("homePosessionPerc {}", homePosessionPerc);
		
		match.setStats(stats);
		match.addEvents(events);
		match.setHomeScore(homeScore);
		match.setAwayScore(awayScore);
		
		rankingService.update(match);
		
		logger.info("{} vs {}", homeScore, awayScore);
		return new int[]{homeScore, awayScore};
	}
	
}
