package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;
import com.lollito.fm.repository.rest.MatchRepository;

@Service
public class MatchService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired UserService userService;
	@Autowired MatchRepository matchRepository;
	
	public List<Match> load(){
		List<Match> matches = new ArrayList<>();
		for(Round round : userService.getLoggedUser().getClub().getLeague().getCurrentSeason().getRounds()){
			matches.addAll(round.getMatches());
		}
		return matches;
	}
	
	public List<Match> loadNext(){
		return loadByRoundNumber(userService.getLoggedUser().getClub().getLeague().getCurrentSeason().getNextRoundNumber() - 1);
	}
	
	public List<Match> loadPrevious(){
		Integer nextRoundNumber = userService.getLoggedUser().getClub().getLeague().getCurrentSeason().getNextRoundNumber();
		return loadByRoundNumber(nextRoundNumber > 1 ? nextRoundNumber - 2 : nextRoundNumber - 1);
	}

	public List<Match> loadUpcomingMatchesForClub() {
		return matchRepository.findUpcomingMatchesByClub(userService.getLoggedUser().getClub().getId());
	}
	
	private List<Match> loadByRoundNumber(Integer number){
		if(userService.getLoggedUser().getClub().getLeague().getCurrentSeason().getRounds().size() <= number) {
			return new ArrayList<>();
		}
		return userService.getLoggedUser().getClub().getLeague().getCurrentSeason().getRounds().get(number).getMatches();
	}

	public Long getCount() {
		return matchRepository.count();
	}
	
	public Match findById(Long id) {
		return matchRepository.findById(id).orElseThrow(() -> new RuntimeException("Match not found: "  + id));
	}
}
