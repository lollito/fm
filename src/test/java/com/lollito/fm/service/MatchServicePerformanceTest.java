package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.RoundRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.UserRepository;

@SpringBootTest
@Transactional
@WithMockUser(username = "testuser")
public class MatchServicePerformanceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired MatchService matchService;
    @Autowired MatchRepository matchRepository;
    @Autowired ClubRepository clubRepository;
    @Autowired SeasonRepository seasonRepository;
    @Autowired RoundRepository roundRepository;
    @Autowired UserRepository userRepository;
    @Autowired CountryRepository countryRepository;
    @Autowired LeagueRepository leagueRepository;
    @Autowired EntityManager entityManager;

    @Test
    public void testLoadNextPerformance() {
        // Setup Country
        Country country = new Country();
        country.setName("Italy");
        country = countryRepository.save(country);

        // Setup League
        League league = new League();
        league.setName("Serie A");
        league.setCountry(country);
        league = leagueRepository.save(league);

        // Setup Season
        Season season = new Season();
        season.setLeague(league);
        season.setCurrent(true);
        season.setNextRoundNumber(1); // Round index 0 is next (since logic passes number-1)
        season = seasonRepository.save(season);

        league.setCurrentSeason(season);
        leagueRepository.save(league);

        // Setup Rounds
        Round round = new Round();
        round.setNumber(1);
        round.setSeason(season);
        round.setMatches(new ArrayList<>());
        round = roundRepository.save(round);

        // We need to ensure season has the round in the list for the service to find it
        // Since we are in the same transaction and relying on JPA, we should update the other side
        season.getRounds().add(round);
        seasonRepository.save(season);

        // Setup Clubs
        List<Club> clubs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Club club = new Club();
            club.setName("Club " + i);
            club.setLeague(league);
            club = clubRepository.save(club);
            clubs.add(club);
        }

        // Setup User (linked to one club)
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setCountry(country);
        user.setClub(clubs.get(0));
        user.setActive(true);
        userRepository.save(user);

        // Setup Matches in Round
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Match match = new Match();
            match.setHome(clubs.get(i * 2));
            match.setAway(clubs.get(i * 2 + 1));
            match.setHomeScore(0);
            match.setAwayScore(0);
            match.setRound(round);
            match.setDate(LocalDateTime.now().plusDays(1));
            match = matchRepository.save(match);
            matches.add(match);

            round.getMatches().add(match);
        }
        roundRepository.save(round);

        // Flush and Clear to ensure N+1 happens
        entityManager.flush();
        entityManager.clear();

        // Measure
        long start = System.nanoTime();

        List<Match> loadedMatches = matchService.loadNext();

        // Trigger lazy loading of clubs (mimic Mapper)
        for (Match m : loadedMatches) {
            m.getHome().getName();
            m.getAway().getName();
        }

        long end = System.nanoTime();
        long duration = (end - start) / 1_000_000; // ms

        logger.info("loadNext duration: {} ms", duration);

        Assertions.assertEquals(10, loadedMatches.size());
        Assertions.assertNotNull(loadedMatches.get(0).getHome().getName());
    }
}
