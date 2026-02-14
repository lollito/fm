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
import com.lollito.fm.model.Injury;
import com.lollito.fm.model.InjuryContext;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchPlayerStats;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.PlayerPosition;
import com.lollito.fm.model.SubstitutionStrategy;
import com.lollito.fm.model.Stats;
import com.lollito.fm.model.dto.MatchResult;
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
	@Autowired StadiumService stadiumService;
	@Autowired PlayerRepository playerRepository;
	@Autowired PlayerHistoryService playerHistoryService;
	@Autowired InjuryService injuryService;
	@Autowired AchievementService achievementService;
	@Autowired ManagerProgressionService managerProgressionService;
	@Autowired StaffService staffService;
	
	public void simulate(List<Match> matches){
		List<Player> allPlayersToSave = new ArrayList<>();
		List<MatchPlayerStats> allMatchStats = new ArrayList<>();
		matches.forEach(match -> {
			simulateMatchLogic(match, null);
			// Collect players
			allPlayersToSave.addAll(match.getHome().getTeam().getPlayers());
			allPlayersToSave.addAll(match.getAway().getTeam().getPlayers());

			// Collect match stats for batch update
			allMatchStats.addAll(match.getPlayerStats());
		});

		playerService.saveAll(allPlayersToSave);

		// Batch update player history stats
		playerHistoryService.updateMatchStatisticsBatch(allMatchStats);

		matchRepository.saveAll(matches);
		rankingService.updateAll(matches);

		matches.forEach(match -> achievementService.checkMatchAchievements(match));
	}
	
	public MatchResult simulate(Match match) {
		return simulate(match, null);
	}

	public MatchResult simulate(Match match, String forcedResult) {
		return simulate(match, forcedResult, true);
	}

	public MatchResult simulate(Match match, String forcedResult, boolean updateRanking) {
		return simulate(match, forcedResult, updateRanking, true);
	}

	public MatchResult simulate(Match match, String forcedResult, boolean updateRanking, boolean saveMatch) {
		simulateMatchLogic(match, forcedResult);

		// Save players
		List<Player> players = Stream.concat(
				match.getHome().getTeam().getPlayers().stream(),
				match.getAway().getTeam().getPlayers().stream())
                .collect(Collectors.toList());
		playerService.saveAll(players);

		// Update player history stats
		match.getPlayerStats().forEach(stats -> playerHistoryService.updateMatchStatistics(stats.getPlayer(), stats));

		matchRepository.save(match);

		if (updateRanking) {
			rankingService.update(match);
		}

		achievementService.checkMatchAchievements(match);

		return MatchResult.builder()
				.matchId(match.getId())
				.homeScore(match.getHomeScore())
				.awayScore(match.getAwayScore())
				.homeTeam(match.getHome().getName())
				.awayTeam(match.getAway().getName())
				.build();
	}

	private void simulateMatchLogic(Match match, String forcedResult) {
		Integer stadiumCapacity = stadiumService.getCapacity(match.getHome().getStadium());
		match.setSpectators(RandomUtils.randomValue(stadiumCapacity/3, stadiumCapacity));
		
		match.getHome().getTeam().setFormation(formationService.createFormation(match.getHome().getTeam().getPlayers(), match.getHome().getTeam().getFormation()));
		match.getAway().getTeam().setFormation((formationService.createFormation(match.getAway().getTeam().getPlayers(), match.getAway().getTeam().getFormation())));
		
		match.setHomeFormation(match.getHome().getTeam().getFormation().copy());
		match.setAwayFormation(match.getAway().getTeam().getFormation().copy());

		playMatch(match);

		if (forcedResult != null) {
			if ("HOME_WIN".equals(forcedResult) && match.getHomeScore() <= match.getAwayScore()) {
				match.setHomeScore(match.getAwayScore() + 1);
			} else if ("AWAY_WIN".equals(forcedResult) && match.getAwayScore() <= match.getHomeScore()) {
				match.setAwayScore(match.getHomeScore() + 1);
			} else if ("DRAW".equals(forcedResult) && !match.getHomeScore().equals(match.getAwayScore())) {
				match.setAwayScore(match.getHomeScore());
			}
		}

		updateMorale(match);

		match.setFinish(true);
		match.setStatus(MatchStatus.COMPLETED);
	}

	public MatchResult simulateMatchWithForcedResult(Match match, String forcedResult) {
		return simulate(match, forcedResult);
	}
	
	/**
	 * Simulates the match logic step-by-step.
	 *
	 * The algorithm follows these phases:
	 * 1. Initialization: Determine number of actions (15-25) and initial possession.
	 * 2. Action Loop: Iterate through each action to determine the outcome.
	 *    - Substitutions: Check for random substitutions at intervals.
	 *    - Possession Phase: The team with the ball attempts to build up play.
	 *    - Midfield Battle: Determine if the attack advances based on team stats + luck.
	 *      - Base probability to advance is increased to reduce stalemate (draws).
	 *    - Finishing Phase: If the attack reaches the final third, a shot is taken.
	 *      - Scoring probability is weighed by Scorer vs Goalkeeper stats.
	 *      - Probabilities adjusted to favour goals slightly more.
	 *    - Defensive Phase: If the attack fails, the defense might tackle or commit a foul.
	 * 3. Condition Update: Decrease player condition based on stamina.
	 *    - Formula updated to be less punishing: Base decay + Stamina factor.
	 * 4. Injury Check: Check for injuries after the match intensity.
	 *
	 * @param match The match to simulate.
	 * @return An array containing [homeScore, awayScore].
	 */
	private int[] playMatch(Match match){
//		Let's say you have an "action" every 5 minutes of the game, so 90/5 = 18 actions. To make it more realistic you can choose random number like:
		// INCREASED ACTIONS (15-25) to allow more scoring opportunities and reduce 0-0 draws.
		Integer numberOfActions = RandomUtils.randomValue(15,25);
		logger.debug("numberOfActions {}", numberOfActions);
		
		Integer homeScore = 0;
		Integer awayScore = 0;
		
		int coin = RandomUtils.randomValue(0, 1);
		Formation homeFormation = match.getHome().getTeam().getFormation();
		Formation awayFormation = match.getAway().getTeam().getFormation();
		List<EventHistory> events = new ArrayList<>();
		Stats stats = new Stats();
		List<Player> cautionedPlayers = new ArrayList<>();

		Map<Player, MatchPlayerStats> allPlayerStats = new HashMap<>();
		for (int i = 0; i < homeFormation.getPlayers().size(); i++) {
			Player p = homeFormation.getPlayers().get(i);
			MatchPlayerStats mps = new MatchPlayerStats();
			mps.setMatch(match);
			mps.setPlayer(p);
			mps.setPosition(getPositionLabel(homeFormation.getModule(), i));
			mps.setStarted(true);
			mps.setMinutesPlayed(90);
			allPlayerStats.put(p, mps);
		}
		for (int i = 0; i < awayFormation.getPlayers().size(); i++) {
			Player p = awayFormation.getPlayers().get(i);
			MatchPlayerStats mps = new MatchPlayerStats();
			mps.setMatch(match);
			mps.setPlayer(p);
			mps.setPosition(getPositionLabel(awayFormation.getModule(), i));
			mps.setStarted(true);
			mps.setMinutesPlayed(90);
			allPlayerStats.put(p, mps);
		}

		if (coin == 0){
			homeFormation.setHaveBall(true);
			events.add(new EventHistory(String.format(Event.HAVE_BALL.getMessage(), match.getHome().getName()) , 0));
			//logger.debug("home have ball");
		} else {
			awayFormation.setHaveBall(true);
			events.add(new EventHistory(String.format(Event.HAVE_BALL.getMessage(), match.getAway().getName()) , 0));
			//logger.debug("away have ball");
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

		Integer stadiumCapacity = stadiumService.getCapacity(match.getHome().getStadium());
		double occupancy = (double) match.getSpectators() / stadiumCapacity;
		int homeAdvantage = (int) (occupancy * 10);

		if (match.getHome().getUser() != null) {
			if (managerProgressionService.hasPerk(match.getHome().getUser(), ManagerPerk.FORTRESS)) {
				homeAdvantage += 2;
			}
		}

		int luckHome = 20 + homeAdvantage;
		int luckAway = 20;
		int homePosession = 0;
		for (int actionNumber = 1; actionNumber <= numberOfActions; actionNumber++) {
			int minute = (90 * actionNumber) / numberOfActions;
			homePosession = homeFormation.getHaveBall() ? homePosession + 1 : homePosession;

			if (actionNumber % 6 == 0 && RandomUtils.randomPercentage(20)) {
				performSubstitution(homeFormation, match, events, minute, allPlayerStats, true);
			}
			if (actionNumber % 7 == 0 && RandomUtils.randomPercentage(20)) {
				performSubstitution(awayFormation, match, events, minute, allPlayerStats, false);
			}

			if(homeFormation.getHaveBall()){
				stats.setHomePasses(stats.getHomePasses() + 1);
				Player passer = RandomUtils.randomValueFromList(homeFormation.getPlayers());
				trackAction(passer, allPlayerStats, ActionType.PASS);
				if (RandomUtils.randomPercentage(75)) {
					stats.setHomeCompletedPasses(stats.getHomeCompletedPasses() + 1);
					trackAction(passer, allPlayerStats, ActionType.COMPLETED_PASS);
				}

				int luck = RandomUtils.randomValue(0, luckHome);
				if(luck > (luckHome/2)){
					luckHome-=RandomUtils.randomValue(0, 3);
					if(luckHome < 0){
						luckHome = 10;
					}
				} else{
					luckHome+=RandomUtils.randomValue(0, 3);
				}
				logger.debug("luck home {}", luck);
				List<Player> hPlayers = homePlayers.get(playerPosition);
				List<Player> aPlayers = awayPlayers.get(inversePosition.get(playerPosition));
				int averageDiff = (playerService.getOffenceAverage(hPlayers) + (hPlayers == null ? 0 : hPlayers.size())) - (playerService.getDefenceAverage(aPlayers) + (aPlayers == null ? 0 : aPlayers.size()));
				logger.debug("home average diff {}", averageDiff);

				// IMPROVED ADVANCEMENT CHANCE: 65% (was 60) and 45% (was 40) base chance.
				if((averageDiff > 0 && RandomUtils.randomPercentage(65 + averageDiff + luck)) || (averageDiff <= 0 && RandomUtils.randomPercentage(45 + averageDiff + luck))) {
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						logger.debug("home ball -> {}", playerPosition.getvalue());
					}else {
							      
						
						Player scorer = RandomUtils.randomValueFromList(homePlayers.get(playerPosition));
						trackAction(scorer, allPlayerStats, ActionType.SHOT);
						
						Integer goalKeeping = awayFormation.getGoalKeeper().getGoalkeepingAverage();
						
						if(scorer.getScoringAverage()  >= goalKeeping) {
							Integer diff = scorer.getScoringAverage() - goalKeeping;
							logger.debug("diff > {}", diff);
							// IMPROVED SCORING CHANCE: 65% (was 60) base chance if Scorer is better.
							if(RandomUtils.randomPercentage(65 + (diff / 2))) {
								homeScore++;
								trackAction(scorer, allPlayerStats, ActionType.GOAL);
								trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
								trackGoalConceded(awayFormation.getGoalKeeper(), allPlayerStats);
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(false);
								awayFormation.setHaveBall(true);
								stats.addHomeShot();
								stats.addHomeOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(), scorer.getSurname()) , minute, Event.HAVE_SCORED, homeScore, awayScore));
								logger.debug("home gol");
							} else {
								stats.addHomeShot();
								stats.addHomeOnTarget();
								trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
								trackSave(awayFormation.getGoalKeeper(), allPlayerStats);
								events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute, Event.HAVE_CORNER));
								logger.debug("home corner");
							}
						} else {
							Integer diff = goalKeeping - scorer.getScoringAverage();
							logger.debug("diff < {}", diff);
							// DECREASED MISS CHANCE: 55% (was 60) base chance to miss if GK is better.
							if(RandomUtils.randomPercentage(55 + (diff / 2))) {
								homeFormation.setHaveBall(false);
								awayFormation.setHaveBall(true);
								playerPosition = inversePosition.get(playerPosition);
								stats.addHomeShot();
								events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute, Event.SHOT_AND_MISSED));
								logger.debug("shot missed");
							} else {
								homeScore++;
								trackAction(scorer, allPlayerStats, ActionType.GOAL);
								trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
								trackGoalConceded(awayFormation.getGoalKeeper(), allPlayerStats);
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(false);
								awayFormation.setHaveBall(true);
								stats.addHomeShot();
								stats.addHomeOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(), scorer.getSurname()) , minute, Event.HAVE_SCORED, homeScore, awayScore));
								logger.debug("home gol");
							}
						}
						
					}
				}else {
					Player tackler = RandomUtils.randomValueFromList(awayPlayers.get(inversePosition.get(playerPosition)));
					stats.setAwayTackles(stats.getAwayTackles() + 1);
					trackAction(tackler, allPlayerStats, ActionType.TACKLE);

					if((averageDiff > 0 && RandomUtils.randomPercentage(40)) || averageDiff <= 0 && RandomUtils.randomPercentage(20)) {
						Player badPlayer = RandomUtils.randomValueFromList(awayPlayers.get(playerPosition));
						stats.addAwayFoul();
						events.add(new EventHistory(String.format(Event.COMMITS_FAUL.getMessage(), badPlayer.getName()) , minute, Event.COMMITS_FAUL));
						if(RandomUtils.randomPercentage(100 - badPlayer.getDefending())) {
							stats.addAwayYellowCard();
							trackAction(badPlayer, allPlayerStats, ActionType.YELLOW_CARD);
							if(cautionedPlayers.contains(badPlayer)) {
								for (PlayerPosition pps : PlayerPosition.values()) {
									awayPlayers.get(pps).remove(badPlayer);
								}
								trackAction(badPlayer, allPlayerStats, ActionType.RED_CARD);
								events.add(new EventHistory(String.format(Event.RED_CARD.getMessage(), badPlayer.getName()) , minute, Event.RED_CARD));
							} else {
								cautionedPlayers.add(badPlayer);
								events.add(new EventHistory(String.format(Event.YELLOW_CARD.getMessage(), badPlayer.getName()) , minute, Event.YELLOW_CARD));
							}
							
						}
						logger.debug("away commits faul {}" , playerPosition);
						if(playerPosition.getvalue() == PlayerPosition.values().length -1 ) {
							Player scorer = homePlayers.get(playerPosition).stream().max(Comparator.comparing(Player::getPiecesAverage)).get();
							trackAction(scorer, allPlayerStats, ActionType.SHOT);

							Integer goalKeeping = awayFormation.getGoalKeeper().getGoalkeepingAverage();
							
							if(scorer.getPiecesAverage()  >= goalKeeping) {
								Integer diff = scorer.getScoringAverage() - goalKeeping;
								logger.debug("diff > {}", diff);
								if(RandomUtils.randomPercentage(40 + (diff / 2))) {
									homeScore++;
									trackAction(scorer, allPlayerStats, ActionType.GOAL);
									trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
									trackGoalConceded(awayFormation.getGoalKeeper(), allPlayerStats);
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									stats.addHomeShot();
									stats.addHomeOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute, Event.HAVE_SCORED_FREE_KICK, homeScore, awayScore));
									logger.debug("home gol free kick");
								} else {
									stats.addHomeShot();
									stats.addHomeOnTarget();
									trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
									trackSave(awayFormation.getGoalKeeper(), allPlayerStats);
									events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute, Event.HAVE_CORNER));
									logger.debug("home corner");
								}
							} else {
								Integer diff = goalKeeping - scorer.getScoringAverage();
								logger.debug("diff < {}", diff);
								if(RandomUtils.randomPercentage(70 + (diff / 2))) {
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									playerPosition = inversePosition.get(playerPosition);
									stats.addHomeShot();
									events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute, Event.SHOT_AND_MISSED));
									logger.debug("shot missed");
								} else {
									homeScore++;
									trackAction(scorer, allPlayerStats, ActionType.GOAL);
									trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
									trackGoalConceded(awayFormation.getGoalKeeper(), allPlayerStats);
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									stats.addHomeShot();
									stats.addHomeOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute, Event.HAVE_SCORED_FREE_KICK, homeScore, awayScore));
									logger.debug("home gol free kick");
								}
							}
						}
					} else {
						logger.debug("home lost ball {}" , playerPosition);
						playerPosition = inversePosition.get(playerPosition);
						homeFormation.setHaveBall(false);
						awayFormation.setHaveBall(true);
						stats.setAwayInterceptions(stats.getAwayInterceptions() + 1);
					}
					
				}
			} else {
				stats.setAwayPasses(stats.getAwayPasses() + 1);
				Player passer = RandomUtils.randomValueFromList(awayFormation.getPlayers());
				trackAction(passer, allPlayerStats, ActionType.PASS);
				if (RandomUtils.randomPercentage(75)) {
					stats.setAwayCompletedPasses(stats.getAwayCompletedPasses() + 1);
					trackAction(passer, allPlayerStats, ActionType.COMPLETED_PASS);
				}

				int luck = RandomUtils.randomValue(0, luckAway);
				if(luck > (luckAway/2)){
					luckAway-=RandomUtils.randomValue(0, 3);
					if(luckAway < 0){
						luckAway = 8;
					}
				} else{
					luckAway+=RandomUtils.randomValue(0, 3);
				}
				logger.debug("luck away {}", luck);
				List<Player> aPlayers = awayPlayers.get(playerPosition);
				List<Player> hPlayers = homePlayers.get(inversePosition.get(playerPosition));
				int averageDiff = (playerService.getOffenceAverage(aPlayers) + (aPlayers == null ? 0 : aPlayers.size())) - (playerService.getDefenceAverage(hPlayers) + (hPlayers == null ? 0 : hPlayers.size()));
				logger.debug("away averageDiff {}", averageDiff);

				// IMPROVED ADVANCEMENT CHANCE
				if((averageDiff > 0 && RandomUtils.randomPercentage(65 + averageDiff + luck)) || (averageDiff <= 0 && RandomUtils.randomPercentage(45 + averageDiff + luck))) {
					if(playerPosition.getvalue() < PlayerPosition.values().length -1   ){
						playerPosition = PlayerPosition.valueOf(playerPosition.getvalue() + 1);
						logger.debug("away ball -> {}", playerPosition.getvalue());
					}else {
						Player scorer = RandomUtils.randomValueFromList(awayPlayers.get(playerPosition));
						trackAction(scorer, allPlayerStats, ActionType.SHOT);
						
						Integer goalKeeping = homeFormation.getGoalKeeper().getGoalkeepingAverage();
						
						if(scorer.getScoringAverage()  >= goalKeeping) {
							Integer diff = scorer.getScoringAverage() - goalKeeping;
							logger.debug("diff > {}", diff);
							// IMPROVED SCORING CHANCE
							if(RandomUtils.randomPercentage(65 + (diff / 2))) {
								awayScore++;
								trackAction(scorer, allPlayerStats, ActionType.GOAL);
								trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
								trackGoalConceded(homeFormation.getGoalKeeper(), allPlayerStats);
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(true);
								awayFormation.setHaveBall(false);
								stats.addAwayShot();
								stats.addAwayOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(),scorer.getSurname()) , minute, Event.HAVE_SCORED, homeScore, awayScore));
								logger.debug("away gol");
							} else {
								stats.addAwayShot();
								stats.addAwayOnTarget();
								trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
								trackSave(homeFormation.getGoalKeeper(), allPlayerStats);
								events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute, Event.HAVE_CORNER));
								logger.debug("home corner");
							}
						} else {
							Integer diff = goalKeeping - scorer.getScoringAverage();
							logger.debug("diff < {}", diff);
							// DECREASED MISS CHANCE
							if(RandomUtils.randomPercentage(55 + (diff / 2))) {
								homeFormation.setHaveBall(true);
								awayFormation.setHaveBall(false);
								playerPosition = inversePosition.get(playerPosition);
								stats.addAwayShot();
								events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute, Event.SHOT_AND_MISSED));
								logger.debug("shot missed");
							} else {
								awayScore++;
								trackAction(scorer, allPlayerStats, ActionType.GOAL);
								trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
								trackGoalConceded(homeFormation.getGoalKeeper(), allPlayerStats);
								playerPosition = PlayerPosition.MIDFIELD;
								homeFormation.setHaveBall(true);
								awayFormation.setHaveBall(false);
								stats.addAwayShot();
								stats.addAwayOnTarget();
								events.add(new EventHistory(String.format(Event.HAVE_SCORED.getMessage(),scorer.getSurname()) , minute, Event.HAVE_SCORED, homeScore, awayScore));
								logger.debug("away gol");
							}
						}
						
					}
				}else{
					Player tackler = RandomUtils.randomValueFromList(homePlayers.get(inversePosition.get(playerPosition)));
					stats.setHomeTackles(stats.getHomeTackles() + 1);
					trackAction(tackler, allPlayerStats, ActionType.TACKLE);

					if((averageDiff > 0 && RandomUtils.randomPercentage(40)) || averageDiff <= 0 && RandomUtils.randomPercentage(20)) {
						Player badPlayer = RandomUtils.randomValueFromList(homePlayers.get(playerPosition));
						stats.addHomeFoul();
						events.add(new EventHistory(String.format(Event.COMMITS_FAUL.getMessage(), badPlayer.getName()) , minute, Event.COMMITS_FAUL));
						if(RandomUtils.randomPercentage(100 - badPlayer.getDefending())) {
							stats.addHomeYellowCard();
							trackAction(badPlayer, allPlayerStats, ActionType.YELLOW_CARD);
							if(cautionedPlayers.contains(badPlayer)) {
								for (PlayerPosition pps : PlayerPosition.values()) {
									homePlayers.get(pps).remove(badPlayer);
								}
								trackAction(badPlayer, allPlayerStats, ActionType.RED_CARD);
								events.add(new EventHistory(String.format(Event.RED_CARD.getMessage(), badPlayer.getName()) , minute, Event.RED_CARD));
							} else {
								cautionedPlayers.add(badPlayer);
								events.add(new EventHistory(String.format(Event.YELLOW_CARD.getMessage(), badPlayer.getName()) , minute, Event.YELLOW_CARD));
							}
						}
						logger.debug("home commits faul {}" , playerPosition);
						if(playerPosition.getvalue() == PlayerPosition.values().length -1 ) {
							Player scorer = awayPlayers.get(playerPosition).stream().max(Comparator.comparing(Player::getPiecesAverage)).get();
							trackAction(scorer, allPlayerStats, ActionType.SHOT);

							Integer goalKeeping = homeFormation.getGoalKeeper().getGoalkeepingAverage();
							
							if(scorer.getPiecesAverage()  >= goalKeeping) {
								Integer diff = scorer.getScoringAverage() - goalKeeping;
								logger.debug("diff > {}", diff);
								if(RandomUtils.randomPercentage(40 + (diff / 2))) {
									awayScore++;
									trackAction(scorer, allPlayerStats, ActionType.GOAL);
									trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
									trackGoalConceded(homeFormation.getGoalKeeper(), allPlayerStats);
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(true);
									awayFormation.setHaveBall(false);
									stats.addAwayShot();
									stats.addAwayOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute, Event.HAVE_SCORED_FREE_KICK, homeScore, awayScore));
									logger.debug("away gol free kick");
								} else {
									stats.addAwayShot();
									stats.addAwayOnTarget();
									trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
									trackSave(homeFormation.getGoalKeeper(), allPlayerStats);
									events.add(new EventHistory(String.format(Event.HAVE_CORNER.getMessage(), match.getHome().getName()) , minute, Event.HAVE_CORNER));
									logger.debug("home corner");
								}
							} else {
								Integer diff = goalKeeping - scorer.getScoringAverage();
								logger.debug("diff < {}", diff);
								if(RandomUtils.randomPercentage(70 + (diff / 2))) {
									homeFormation.setHaveBall(false);
									awayFormation.setHaveBall(true);
									playerPosition = inversePosition.get(playerPosition);
									stats.addAwayShot();
									events.add(new EventHistory(String.format(Event.SHOT_AND_MISSED.getMessage(), scorer.getSurname()) , minute, Event.SHOT_AND_MISSED));
									logger.debug("shot missed");
								} else {
									awayScore++;
									trackAction(scorer, allPlayerStats, ActionType.GOAL);
									trackAction(scorer, allPlayerStats, ActionType.SHOT_ON_TARGET);
									trackGoalConceded(homeFormation.getGoalKeeper(), allPlayerStats);
									playerPosition = PlayerPosition.MIDFIELD;
									homeFormation.setHaveBall(true);
									awayFormation.setHaveBall(false);
									stats.addAwayShot();
									stats.addAwayOnTarget();
									events.add(new EventHistory(String.format(Event.HAVE_SCORED_FREE_KICK.getMessage(), scorer.getSurname()) , minute, Event.HAVE_SCORED_FREE_KICK, homeScore, awayScore));
									logger.debug("away gol free kick");
								}
							}
						}
					} else {
						logger.debug("away lost ball {}" , playerPosition);
						playerPosition = inversePosition.get(playerPosition);
						homeFormation.setHaveBall(true);
						awayFormation.setHaveBall(false);
						stats.setHomeInterceptions(stats.getHomeInterceptions() + 1);
					}
				}
			}

			// NEW CONDITION DECAY LOGIC
			// Adjusted formula to be less aggressive.
			// Old: 5 - (5 * stamina/100) -> linear 0 to 5 drop per action.
			// New: 0.5 + (1.5 * (100-stamina)/100) -> 0.5 to 2.0 drop per action.
			for(Player player : homeFormation.getPlayers()) {
			double d = 0.5 + (1.5 * (100 - player.getStamina()) / 100.0);
	        	player.decrementCondition(d);
	        }
			for(Player player : awayFormation.getPlayers()) {
				double d = 0.5 + (1.5 * (100 - player.getStamina()) / 100.0);
	        	player.decrementCondition(d);
	        }
		}
		double matchIntensity = 1.0;

		Double homeInjuryBonus = 0.0;
		if (match.getHome() != null) {
			Double bonus = staffService.calculateClubStaffBonuses(match.getHome()).getInjuryPreventionBonus();
			if (bonus != null) homeInjuryBonus = bonus;
		}

		Double awayInjuryBonus = 0.0;
		if (match.getAway() != null) {
			Double bonus = staffService.calculateClubStaffBonuses(match.getAway()).getInjuryPreventionBonus();
			if (bonus != null) awayInjuryBonus = bonus;
		}

		for (Player player : homeFormation.getPlayers()) {
			if (injuryService.checkForInjury(player, matchIntensity, homeInjuryBonus)) {
				Injury injury = injuryService.createInjury(player, InjuryContext.MATCH);
				int injuryMinute = RandomUtils.randomValue(1, 90);
				events.add(new EventHistory(String.format(Event.INJURY.getMessage(), player.getSurname(), injury.getDescription()), injuryMinute, Event.INJURY));
			}
		}

		for (Player player : awayFormation.getPlayers()) {
			if (injuryService.checkForInjury(player, matchIntensity, awayInjuryBonus)) {
				Injury injury = injuryService.createInjury(player, InjuryContext.MATCH);
				int injuryMinute = RandomUtils.randomValue(1, 90);
				events.add(new EventHistory(String.format(Event.INJURY.getMessage(), player.getSurname(), injury.getDescription()), injuryMinute, Event.INJURY));
			}
		}
		events.sort(Comparator.comparingInt(EventHistory::getMinute));

		int homePosessionPerc = (homePosession * 100) / numberOfActions;
		stats.setHomePossession(homePosessionPerc);
		stats.setAwayPossession(100 - homePosessionPerc);
		
		logger.debug("homePosessionPerc {}", homePosessionPerc);
		
		match.setStats(stats);
		match.addEvents(events);
		match.setHomeScore(homeScore);
		match.setAwayScore(awayScore);

		allPlayerStats.values().forEach(mps -> {
			double rating = 6.0;
			rating += mps.getGoals() * 1.5;
			rating += mps.getAssists() * 1.0;
			rating += (mps.getShotsOnTarget() / 5.0);
			rating += (mps.getCompletedPasses() / 20.0);
			rating += (mps.getTackles() / 10.0);
			rating -= mps.getYellowCards() * 0.5;
			rating -= mps.getRedCards() * 2.0;
			if (rating > 10.0) rating = 10.0;
			if (rating < 1.0) rating = 1.0;
			mps.setRating(rating);
		});

		MatchPlayerStats mvp = allPlayerStats.values().stream()
				.max(Comparator.comparing(MatchPlayerStats::getRating))
				.orElse(null);
		if (mvp != null) mvp.setMvp(true);

		match.setPlayerStats(new ArrayList<>(allPlayerStats.values()));
		
		logger.debug("{} vs {}", homeScore, awayScore);
		return new int[]{homeScore, awayScore};
	}

	private void trackAction(Player player, Map<Player, MatchPlayerStats> allPlayerStats, ActionType actionType) {
		MatchPlayerStats mps = allPlayerStats.get(player);
		if (mps == null) return;
		switch (actionType) {
			case SHOT: mps.setShots(mps.getShots() + 1); break;
			case SHOT_ON_TARGET: mps.setShotsOnTarget(mps.getShotsOnTarget() + 1); break;
			case GOAL: mps.setGoals(mps.getGoals() + 1); break;
			case PASS: mps.setPasses(mps.getPasses() + 1); break;
			case COMPLETED_PASS: mps.setCompletedPasses(mps.getCompletedPasses() + 1); break;
			case TACKLE: mps.setTackles(mps.getTackles() + 1); break;
			case YELLOW_CARD: mps.setYellowCards(mps.getYellowCards() + 1); break;
			case RED_CARD: mps.setRedCards(mps.getRedCards() + 1); break;
		}
	}

	private void trackSave(Player gk, Map<Player, MatchPlayerStats> allPlayerStats) {
		if (gk == null) return;
		MatchPlayerStats mps = allPlayerStats.get(gk);
		if (mps != null) {
			mps.setSaves(mps.getSaves() + 1);
		}
	}

	private void trackGoalConceded(Player gk, Map<Player, MatchPlayerStats> allPlayerStats) {
		if (gk == null) return;
		MatchPlayerStats mps = allPlayerStats.get(gk);
		if (mps != null) {
			mps.setGoalsConceded(mps.getGoalsConceded() + 1);
		}
	}

	private void performSubstitution(Formation formation, Match match, List<EventHistory> events, int minute, Map<Player, MatchPlayerStats> allPlayerStats, boolean isHome) {
		if (formation.getSubstitutes() == null || formation.getSubstitutes().isEmpty()) {
			return;
		}

		SubstitutionStrategy strategy = formation.getSubstitutionStrategy();
		if (strategy == null) strategy = SubstitutionStrategy.AUTO;

		// Identify candidate OUT
		Player candidateOut = null;
		double maxScore = -1.0;

		int homeScore = match.getHomeScore();
		int awayScore = match.getAwayScore();
		boolean isWinning = isHome ? homeScore > awayScore : awayScore > homeScore;
		boolean isLosing = isHome ? homeScore < awayScore : awayScore < homeScore;
		int scoreDiff = Math.abs(homeScore - awayScore);
		boolean comfortableWin = isWinning && scoreDiff >= 2;

		for (Player p : formation.getPlayers()) {
			if (p.getRole() == PlayerRole.GOALKEEPER) continue;

			MatchPlayerStats stats = allPlayerStats.get(p);
			if (stats == null) continue;

			double score = 0.0;

			// Fatigue
			if (p.getCondition() != null) {
				score += (100.0 - p.getCondition()) * 1.5;
			}

			// Rating
			if (stats.getRating() != null) {
				score += (10.0 - stats.getRating()) * 10.0;
			}

			// Yellow Card Risk
			if (stats.getYellowCards() > 0) {
				if (comfortableWin) {
					score += 50.0;
				} else if (strategy == SubstitutionStrategy.AGGRESSIVE || strategy == SubstitutionStrategy.DEFENSIVE) {
					score += 20.0;
				}
			}

			// Tactics
			if (isLosing) {
				if (strategy == SubstitutionStrategy.AGGRESSIVE || strategy == SubstitutionStrategy.AUTO) {
					if (p.getRole() == PlayerRole.DEFENDER || p.getRole() == PlayerRole.WINGBACK) {
						score += 15.0;
					}
				}
			} else if (isWinning) {
				if (strategy == SubstitutionStrategy.DEFENSIVE || strategy == SubstitutionStrategy.AUTO) {
					if (p.getRole() == PlayerRole.FORWARD || p.getRole() == PlayerRole.WING) {
						score += 15.0;
					}
				}
			}

			if (score > maxScore) {
				maxScore = score;
				candidateOut = p;
			}
		}

		if (candidateOut == null) {
			// Fallback: random if no clear candidate found (unlikely given score logic)
			candidateOut = RandomUtils.randomValueFromList(formation.getPlayers());
		}

		// Identify candidate IN
		Player candidateIn = null;
		List<Player> availableSubs = new ArrayList<>(formation.getSubstitutes());

		if (isLosing && (strategy == SubstitutionStrategy.AGGRESSIVE || strategy == SubstitutionStrategy.AUTO)) {
			if (candidateOut.getRole() == PlayerRole.DEFENDER || candidateOut.getRole() == PlayerRole.WINGBACK) {
				candidateIn = findBestSubForRole(availableSubs, PlayerRole.FORWARD);
				if (candidateIn == null) candidateIn = findBestSubForRole(availableSubs, PlayerRole.WING);
			}
		} else if (isWinning && (strategy == SubstitutionStrategy.DEFENSIVE || strategy == SubstitutionStrategy.AUTO)) {
			if (candidateOut.getRole() == PlayerRole.FORWARD || candidateOut.getRole() == PlayerRole.WING) {
				candidateIn = findBestSubForRole(availableSubs, PlayerRole.DEFENDER);
				if (candidateIn == null) candidateIn = findBestSubForRole(availableSubs, PlayerRole.WINGBACK);
			}
		}

		if (candidateIn == null) {
			candidateIn = findBestSubForRole(availableSubs, candidateOut.getRole());
		}

		if (candidateIn == null && !availableSubs.isEmpty()) {
			candidateIn = availableSubs.get(0);
		}

		if (candidateIn != null) {
			formation.getSubstitutes().remove(candidateIn);
			int idx = formation.getPlayers().indexOf(candidateOut);
			if (idx != -1) {
				formation.getPlayers().set(idx, candidateIn);

				MatchPlayerStats mpsIn = new MatchPlayerStats();
				mpsIn.setMatch(match);
				mpsIn.setPlayer(candidateIn);
				mpsIn.setStarted(false);
				mpsIn.setMinutesPlayed(90 - minute);

				MatchPlayerStats mpsOut = allPlayerStats.get(candidateOut);
				if (mpsOut != null) {
					mpsIn.setPosition(mpsOut.getPosition());
					mpsOut.setMinutesPlayed(minute);
				}
				allPlayerStats.put(candidateIn, mpsIn);

				events.add(new EventHistory(String.format(Event.SUBSTITUTION.getMessage(), candidateIn.getSurname(), candidateOut.getSurname()), minute, Event.SUBSTITUTION));
				logger.debug("{} Substitution: {} for {} (Strategy: {})", isHome ? "Home" : "Away", candidateIn.getSurname(), candidateOut.getSurname(), strategy);
			}
		}
	}

	private Player findBestSubForRole(List<Player> subs, PlayerRole role) {
		return subs.stream()
				.filter(p -> p.getRole() == role)
				.max(Comparator.comparing(Player::getAverage))
				.orElse(null);
	}

	private String getPositionLabel(Module module, int index) {
		if (index == 0) return "GK";
		if (index <= module.getCd()) return "CD";
		if (index <= module.getCd() + module.getWb()) return "WB";
		if (index <= module.getCd() + module.getWb() + module.getMf()) return "MF";
		if (index <= module.getCd() + module.getWb() + module.getMf() + module.getWng()) return "WNG";
		return "FW";
	}

	private enum ActionType {
		SHOT, SHOT_ON_TARGET, GOAL, PASS, COMPLETED_PASS, TACKLE, YELLOW_CARD, RED_CARD
	}

	private void updateMorale(Match match) {
		boolean homeWin = match.getHomeScore() > match.getAwayScore();
		boolean awayWin = match.getAwayScore() > match.getHomeScore();
		boolean draw = match.getHomeScore().equals(match.getAwayScore());

		// Home Team
		for (Player p : match.getHome().getTeam().getPlayers()) {
			if (homeWin) {
				p.incrementMoral(2.0);
			} else if (draw) {
				p.incrementMoral(0.5);
			} else {
				double decrement = 5.0;
				if (match.getHome().getUser() != null && managerProgressionService.hasPerk(match.getHome().getUser(), ManagerPerk.MOTIVATOR)) {
					decrement *= 0.9;
				}
				p.decrementMoral(decrement);
			}
		}

		// Away Team
		for (Player p : match.getAway().getTeam().getPlayers()) {
			if (awayWin) {
				p.incrementMoral(2.0);
			} else if (draw) {
				p.incrementMoral(0.5);
			} else {
				double decrement = 5.0;
				if (match.getAway().getUser() != null && managerProgressionService.hasPerk(match.getAway().getUser(), ManagerPerk.MOTIVATOR)) {
					decrement *= 0.9;
				}
				p.decrementMoral(decrement);
			}
		}
	}
	
}
