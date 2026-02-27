package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.lollito.fm.model.User;
import com.lollito.fm.model.League;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.RankingRepository;
import com.lollito.fm.repository.rest.RoundRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.LeagueRepository;

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

	@Test
	public void testLoadPerformance() {
		// Setup League and Season
		League league = new League();
		league.setName("Test League");
		league = leagueRepository.save(league);

		Season season = new Season();
		season.setName("Test Season");
		season.setLeague(league);
		season = seasonRepository.save(season);
		league.setCurrentSeason(season);
		league = leagueRepository.save(league);

		// Handle existing user
		String username = "lollito";
		Optional<User> existingUser = userRepository.findByUsername(username);
		User user;
		if (existingUser.isPresent()) {
			user = existingUser.get();
			// Ensure clean state for test
			user.setClub(null);
		} else {
			user = new User();
			user.setUsername(username);
			user.setActive(true);
		}

		Club userClub = new Club();
		userClub.setName("User Club");
		userClub.setLeague(league);
		userClub = clubRepository.save(userClub);

		user.setClub(userClub);
		user = userRepository.save(user);

		// Setup other Clubs and Rankings
		List<Club> clubs = new ArrayList<>();
		clubs.add(userClub);

		// Add user club ranking
		Ranking userClubRanking = new Ranking();
		userClubRanking.setClub(userClub);
		userClubRanking.setSeason(season);
		userClubRanking.setPoints(10);
		rankingRepository.save(userClubRanking);
		season.addRankingLine(userClubRanking);

		// Add 19 other clubs
		for (int i = 0; i < 19; i++) {
			Club club = new Club();
			club.setName("Club " + i);
			club.setLeague(league);
			club = clubRepository.save(club);
			clubs.add(club);

			Ranking ranking = new Ranking();
			ranking.setClub(club);
			ranking.setSeason(season);
			ranking.setPoints(i); // Vary points
			rankingRepository.save(ranking);
			season.addRankingLine(ranking);
		}

		season = seasonRepository.save(season);

		// Measure
		long start = System.nanoTime();

		List<Ranking> rankings = rankingService.load();

		long end = System.nanoTime();
		long duration = (end - start) / 1_000_000; // ms

		logger.info("testLoadPerformance duration: {} ms", duration);

		// Verify
		Assertions.assertNotNull(rankings);
		Assertions.assertEquals(20, rankings.size());
	}
}
