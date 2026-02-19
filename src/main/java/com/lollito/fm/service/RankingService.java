package com.lollito.fm.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.RankingRepository;
import java.util.Collections;

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
		Ranking rankingLineHome = rankingLineRepository.findFirstByClubAndSeason(match.getHome(), match.getRound().getSeason());
		if (rankingLineHome != null) {
			rankingLineHome.updateStats(match.getHomeScore(), match.getAwayScore());
			rankingLineRepository.save(rankingLineHome);
		}
		
		Ranking rankingLineAway = rankingLineRepository.findFirstByClubAndSeason(match.getAway(), match.getRound().getSeason());
		if (rankingLineAway != null) {
			rankingLineAway.updateStats(match.getAwayScore(), match.getHomeScore());
			rankingLineRepository.save(rankingLineAway);
		}
	}
	
	public void updateAll(List<Match> matches) {
		if (matches == null || matches.isEmpty()) {
			return;
		}

		Season season = matches.get(0).getRound().getSeason();

		List<Ranking> rankings = rankingLineRepository.findBySeason(season);
		Map<Long, Ranking> rankingMap = rankings.stream()
				.collect(Collectors.toMap(r -> r.getClub().getId(), r -> r));

		Set<Ranking> modifiedRankings = new HashSet<>();

		for (Match match : matches) {
			Ranking rankingLineHome = rankingMap.get(match.getHome().getId());
			if (rankingLineHome != null) {
				rankingLineHome.updateStats(match.getHomeScore(), match.getAwayScore());
				modifiedRankings.add(rankingLineHome);
			}

			Ranking rankingLineAway = rankingMap.get(match.getAway().getId());
			if (rankingLineAway != null) {
				rankingLineAway.updateStats(match.getAwayScore(), match.getHomeScore());
				modifiedRankings.add(rankingLineAway);
			}
		}

		rankingLineRepository.saveAll(modifiedRankings);
	}

	public List<Ranking> load(){
		User user = userService.getLoggedUser();
		if (user != null && user.getClub() != null) {
			Season season = user.getClub().getLeague().getCurrentSeason();
			return rankingLineRepository.findBySeasonOrderByPointsDesc(season);
		}
		return Collections.emptyList();
	}
}
