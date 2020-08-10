package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.repository.rest.RankingRepository;

@Service
public class RankingService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired RankingRepository rankingLineRepository;
	@Autowired UserService userService;
	
	public void create(List<Club> clubs, Season season) {
		for (Club club : clubs) {
			Ranking rankingLine = new Ranking();
			rankingLine.setClub(club);
			season.addRankingLine(rankingLine);
		}
	}
	
	public void update(Match match){
		Ranking rankingLineHome = rankingLineRepository.findByClubAndSeason(match.getHome(), match.getRound().getSeason());
		rankingLineHome.updateStats(match.getHomeScore(), match.getAwayScore());
		rankingLineRepository.save(rankingLineHome);
		
		Ranking rankingLineAway = rankingLineRepository.findByClubAndSeason(match.getAway(), match.getRound().getSeason());
		rankingLineAway.updateStats(match.getAwayScore(), match.getHomeScore());
		rankingLineRepository.save(rankingLineAway);
	}
	
	public List<Ranking> load(){
		return userService.getLoggedUser().getClub().getLeague().getCurrentSeason().getRankingLines();
	}
}
