package com.lollito.fm.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.dto.SeasonAdvancementResult;
import com.lollito.fm.repository.rest.SeasonRepository;

@Service
public class SeasonService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired SeasonRepository seasonRepository;
	@Autowired RankingService rankingService;
	@Autowired SimulationMatchService simulationMatchService;
	@Autowired LeagueService leagueService;
	@Autowired AchievementService achievementService;
	
	public Season create(League league, LocalDateTime startDate) {
		// Deactivate previous current season
		List<Season> currentSeasons = seasonRepository.findAllByCurrentTrue();
		for (Season s : currentSeasons) {
			// Check achievements for the ending season
			achievementService.checkSeasonAchievements(s);

			s.setCurrent(false);
			seasonRepository.save(s);
		}

		Season season = new Season();
		season.setStartYear(startDate.getYear());
		season.setEndYear(startDate.getYear() + 1);
		season.setCurrent(true);

		List<Club> clubsList = league.getClubs();
		rankingService.create(clubsList, season);
		int numClubs = clubsList.size();
		if (numClubs % 2 != 0) {
			logger.error("numClubs % 2 exception");
			throw new RuntimeException("error");
		}

		int numDays = (numClubs - 1); 
		int halfSize = numClubs / 2;

		List<Club> clubs = new ArrayList<>();
		clubs.addAll(clubsList); 
		clubs.remove(0);

		int teamsSize = clubs.size();
		List<Round> rounds = new ArrayList<>();
		
		for (int day = 0; day < numDays; day++) {
			Round round = new Round();
			int teamIdx = day % teamsSize;

			round.addMatch(new Match(clubs.get(teamIdx), clubsList.get(0), false));
			for (int idx = 1; idx < halfSize; idx++) {
				int firstTeam = (day + idx) % teamsSize;
				int secondTeam = (day + teamsSize - idx) % teamsSize;
				boolean last = idx == halfSize - 1;
				round.addMatch(new Match(clubs.get(firstTeam), clubs.get(secondTeam), last));
			}
			rounds.add(round);
		}
		Collections.shuffle(rounds);
		
		List<Round> roundReturns = new ArrayList<>();
		for (Round round : rounds) {
			Round roundReturn = new Round();
			for (int i = 0; i < round.getMatches().size(); i++) {
				Match match = round.getMatches().get(i);
				boolean last = i == round.getMatches().size() - 1;
				roundReturn.addMatch(new Match(match.getAway(), match.getHome(), last));
			}
			roundReturns.add(roundReturn);
		}
		rounds.addAll(roundReturns);
		minCalendar(startDate, season, rounds);
		season.getRounds().get(season.getRounds().size() -1).setLast(Boolean.TRUE);
		return season;
	}

	private void minCalendar(LocalDateTime startDate, Season season, List<Round> rounds) {
		int roundNumber = 1;
		for(Round round : rounds) {
			for(Match match: round.getMatches()) {
				match.setDate(startDate);
				
			}
			round.setNumber(roundNumber);
			season.addRound(round);
			roundNumber ++;
			startDate = startDate.plusMinutes(10);
		}
	}
	
	private void realisticCalendar(LocalDateTime startDate, Season season, List<Round> rounds) {
		int roundNumber = 1;
		int saturdayMatch = 2;
		Iterator<Round> iterator = rounds.iterator();
		while (iterator.hasNext()) {
			if(startDate.getDayOfWeek() == DayOfWeek.SATURDAY){
				Round round = iterator.next();
				round.setNumber(roundNumber);
				List<Match> matches = round.getMatches();
				for(int i= 0; i < saturdayMatch; i++){
					matches.get(i).setDate(startDate);
				}
				startDate = startDate.plusDays(1);
				for(int i= saturdayMatch; i < matches.size(); i++){
					matches.get(i).setDate(startDate);
				}
				roundNumber ++;
				season.addRound(round);
			}
			startDate = startDate.plusDays(1);
		}
	}

	public Season getCurrentSeason() {
		List<Season> seasons = seasonRepository.findAllByCurrentTrue();
		return seasons.isEmpty() ? null : seasons.get(0);
	}

	public Season findById(Long id) {
		return seasonRepository.findById(id).orElse(null);
	}

	@Transactional
	public SeasonAdvancementResult forceAdvanceSeason(boolean skipRemainingMatches, boolean generateNewPlayers, boolean processTransfers) {
		Season currentSeason = getCurrentSeason();
		int matchesProcessed = 0;

		if (skipRemainingMatches && currentSeason != null) {
			List<Round> rounds = currentSeason.getRounds();
			for (Round round : rounds) {
				for (Match match : round.getMatches()) {
					if (!match.getFinish()) {
						simulationMatchService.simulate(match);
						matchesProcessed++;
					}
				}
			}
		}

		if (currentSeason != null) {
			League league = currentSeason.getLeague();
			Season newSeason = create(league, LocalDateTime.now().plusMinutes(10));
			league.setCurrentSeason(newSeason);
			leagueService.save(league);

			return SeasonAdvancementResult.builder()
					.matchesProcessed(matchesProcessed)
					.transfersProcessed(0)
					.seasonAdvanced(true)
					.build();
		}

		return SeasonAdvancementResult.builder()
				.matchesProcessed(0)
				.transfersProcessed(0)
				.seasonAdvanced(false)
				.build();
	}
}
