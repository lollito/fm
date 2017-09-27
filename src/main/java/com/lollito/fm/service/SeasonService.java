package com.lollito.fm.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.repository.rest.SeasonRepository;

@Service
public class SeasonService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired SeasonRepository seasonRepository;
	@Autowired RankingService rankingService;
	
	public Season create(List<Club> clubsList, Game game) {
		Season season = new Season();
		season.setRanking(rankingService.create(clubsList));
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
			//logger.info("Day {}", (day + 1));
			
			Round round = new Round();
			int teamIdx = day % teamsSize;

			//logger.info("{} vs {}", teams.get(teamIdx).getName(), teamsList.get(0).getName());
			round.addMatch(new Match(clubs.get(teamIdx), clubsList.get(0), game));
			for (int idx = 1; idx < halfSize; idx++) {
				int firstTeam = (day + idx) % teamsSize;
				int secondTeam = (day + teamsSize - idx) % teamsSize;
				//logger.info("{} vs {}", teams.get(firstTeam).getName(), teams.get(secondTeam).getName());
				round.addMatch(new Match(clubs.get(firstTeam), clubs.get(secondTeam), game));
			}
			rounds.add(round);
		}
		Collections.shuffle(rounds);
		
		List<Round> roundReturns = new ArrayList<>();
		for (Round round : rounds) {
			Round roundReturn = new Round();
			for (Match match : round.getMatches()) {
				roundReturn.addMatch(new Match(match.getAway(), match.getHome(), game));
			}
			roundReturns.add(roundReturn);
		}
		rounds.addAll(roundReturns);
		int roundNumber = 1;
		LocalDate date = LocalDate.of( 2017 , Month.AUGUST , 21 );
		int saturdayMatch = 2;
		Iterator<Round> iterator = rounds.iterator();
		while (iterator.hasNext()) {
			if(date.getDayOfWeek() == DayOfWeek.SATURDAY){
				Round round = iterator.next();
				round.setNumber(roundNumber);
				List<Match> matches = round.getMatches();
				for(int i= 0; i < saturdayMatch; i++){
					matches.get(i).setDate(date);
				}
				date = date.plusDays(1);
				for(int i= saturdayMatch; i < matches.size(); i++){
					matches.get(i).setDate(date);
				}
				roundNumber ++;
				season.addRound(round);
			}
			date = date.plusDays(1);
		}
		return season;
	}
}
