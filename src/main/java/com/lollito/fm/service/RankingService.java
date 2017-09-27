package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.RankingLine;
import com.lollito.fm.repository.rest.RankingLineRepository;

@Service
public class RankingService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired RankingLineRepository rankingLineRepository;
	
	public Ranking create(List<Club> clubs) {
		Ranking ranking = new Ranking();
		for (Club club : clubs) {
			RankingLine rankingLine = new RankingLine();
			rankingLine.setClub(club);
			ranking.addRankingLine(rankingLine);
		}
		return ranking;
	}
	
	public void update(Match match){
		RankingLine rankingLineHome = rankingLineRepository.findByClubAndRanking(match.getHome(), match.getRound().getSeason().getRanking());
		rankingLineHome.updateStats(match.getHomeScore(), match.getAwayScore());
		rankingLineRepository.save(rankingLineHome);
		
		RankingLine rankingLineAway = rankingLineRepository.findByClubAndRanking(match.getAway(), match.getRound().getSeason().getRanking());
		rankingLineAway.updateStats(match.getAwayScore(), match.getHomeScore());
		rankingLineRepository.save(rankingLineAway);
	}
}
