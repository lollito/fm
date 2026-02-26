package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.RankingRepository;
import com.lollito.fm.repository.rest.RoundRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.model.User;
import com.lollito.fm.model.League;

@SpringBootTest
@Transactional
@WithMockUser(username = "lollito")
public class RankingServicePerformanceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired RankingService rankingService;
	@Autowired RankingRepository rankingRepository;
	@Autowired ClubRepository clubRepository;
	@Autowired SeasonRepository seasonRepository;
	@Autowired MatchRepository matchRepository;
	@Autowired RoundRepository roundRepository;
	@Autowired UserRepository userRepository;
	@Autowired LeagueRepository leagueRepository;

	@Test
	public void testLoad() {
		// Create User if not exists
		User user = userRepository.findByUsername("lollito").orElse(null);
		if (user == null) {
			user = new User();
			user.setUsername("lollito");
			user.setEmail("lollito@example.com");
			user = userRepository.save(user);
		}

		// Create League
		League league = new League();
		league.setName("Test League");
		league = leagueRepository.save(league);

		// Create Season
		Season season = new Season();
		season.setName("2024");
		season.setCurrent(true);
		season.setLeague(league);
		season = seasonRepository.save(season);

		// Update League with Season (inverse side)
		league.setCurrentSeason(season);
		leagueRepository.save(league);

		// Create Club for User
		Club club = new Club();
		club.setName("User Club");
		club.setLeague(league);
		club.setUser(user);
		club = clubRepository.save(club);

		// Update User with Club
		user.setClub(club);
		userRepository.save(user);

		// Create Rankings
		for (int i = 0; i < 5; i++) {
			Club c = new Club();
			c.setName("Club " + i);
			c = clubRepository.save(c);

			Ranking ranking = new Ranking();
			ranking.setClub(c);
			ranking.setSeason(season);
			ranking.setPoints(i * 10); // 0, 10, 20, 30, 40
			rankingRepository.save(ranking);
		}

		// Add user's club to ranking
		Ranking userRanking = new Ranking();
		userRanking.setClub(club);
		userRanking.setSeason(season);
		userRanking.setPoints(25);
		rankingRepository.save(userRanking);

		// Test load
		List<Ranking> rankings = rankingService.load();

		Assertions.assertNotNull(rankings);
		Assertions.assertEquals(6, rankings.size());

		// Verify Order (Points Descending)
		Assertions.assertEquals(40, rankings.get(0).getPoints());
		Assertions.assertEquals(30, rankings.get(1).getPoints());
		Assertions.assertEquals(25, rankings.get(2).getPoints()); // User club
		Assertions.assertEquals(20, rankings.get(3).getPoints());
	}

	@Test
	public void testUpdateLoop() {
		// Setup
		Season season = new Season();
		season = seasonRepository.save(season);

		List<Club> clubs = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			Club club = new Club();
			club.setName("Club " + i);
			club = clubRepository.save(club);
			clubs.add(club);

			Ranking ranking = new Ranking();
			ranking.setClub(club);
			ranking.setSeason(season);
			rankingRepository.save(ranking);
		}

		Round round = new Round();
		round.setSeason(season);
		round = roundRepository.save(round);

		List<Match> matches = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Match match = new Match();
			match.setHome(clubs.get(i * 2));
			match.setAway(clubs.get(i * 2 + 1));
			match.setHomeScore(1);
			match.setAwayScore(0);
			match.setRound(round);
			match = matchRepository.save(match);
			matches.add(match);
		}

		// Measure
		long start = System.nanoTime();

		for (Match match : matches) {
			rankingService.update(match);
		}

		long end = System.nanoTime();
		long duration = (end - start) / 1_000_000; // ms

		logger.info("testUpdateLoop duration: {} ms", duration);

		// Verify correctness for first match
		Ranking homeRanking = rankingRepository.findByClubAndSeason(clubs.get(0), season);
		Ranking awayRanking = rankingRepository.findByClubAndSeason(clubs.get(1), season);

		Assertions.assertEquals(1, homeRanking.getWon());
		Assertions.assertEquals(3, homeRanking.getPoints());
		Assertions.assertEquals(1, homeRanking.getGoalsFor());

		Assertions.assertEquals(1, awayRanking.getLost());
		Assertions.assertEquals(0, awayRanking.getPoints());
		Assertions.assertEquals(1, awayRanking.getGoalAgainst());
	}

	@Test
	public void testUpdateAll() {
		// Setup
		Season season = new Season();
		season = seasonRepository.save(season);

		List<Club> clubs = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			Club club = new Club();
			club.setName("Club " + i);
			club = clubRepository.save(club);
			clubs.add(club);

			Ranking ranking = new Ranking();
			ranking.setClub(club);
			ranking.setSeason(season);
			rankingRepository.save(ranking);
		}

		Round round = new Round();
		round.setSeason(season);
		round = roundRepository.save(round);

		List<Match> matches = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Match match = new Match();
			match.setHome(clubs.get(i * 2));
			match.setAway(clubs.get(i * 2 + 1));
			match.setHomeScore(1);
			match.setAwayScore(0);
			match.setRound(round);
			match = matchRepository.save(match);
			matches.add(match);
		}

		// Measure
		long start = System.nanoTime();

		rankingService.updateAll(matches);

		long end = System.nanoTime();
		long duration = (end - start) / 1_000_000; // ms

		logger.info("testUpdateAll duration: {} ms", duration);

		// Verify correctness for first match
		Ranking homeRanking = rankingRepository.findByClubAndSeason(clubs.get(0), season);
		Ranking awayRanking = rankingRepository.findByClubAndSeason(clubs.get(1), season);

		Assertions.assertEquals(1, homeRanking.getWon());
		Assertions.assertEquals(3, homeRanking.getPoints());
		Assertions.assertEquals(1, homeRanking.getGoalsFor());

		Assertions.assertEquals(1, awayRanking.getLost());
		Assertions.assertEquals(0, awayRanking.getPoints());
		Assertions.assertEquals(1, awayRanking.getGoalAgainst());
	}
}
