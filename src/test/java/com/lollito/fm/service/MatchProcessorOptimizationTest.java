package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Formation;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.repository.rest.FormationRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.RoundRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.StadiumRepository;
import com.lollito.fm.repository.rest.TeamRepository;

@SpringBootTest
@Transactional
@WithMockUser(username = "testuser")
public class MatchProcessorOptimizationTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired MatchProcessor matchProcessor;
    @Autowired MatchRepository matchRepository;
    @Autowired ClubRepository clubRepository;
    @Autowired SeasonRepository seasonRepository;
    @Autowired RoundRepository roundRepository;
    @Autowired CountryRepository countryRepository;
    @Autowired LeagueRepository leagueRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired PlayerRepository playerRepository;
    @Autowired FormationRepository formationRepository;
    @Autowired ModuleRepository moduleRepository;
    @Autowired ModuleService moduleService;
    @Autowired MentalityService mentalityService;
    @Autowired StadiumRepository stadiumRepository;
    @Autowired EntityManager entityManager;

    @Test
    public void testFindByIdWithSimulationData() {
        // Setup modules if not exists
        if (moduleRepository.count() == 0) {
            moduleService.createModules();
        }
        Module defaultModule = moduleRepository.findAll().get(0);

        // Setup Country
        Country country = new Country();
        country.setName("SimCountry");
        country = countryRepository.save(country);

        // Setup League
        League league = new League();
        league.setName("Sim League");
        league.setCountry(country);
        league = leagueRepository.save(league);

        // Setup Season
        Season season = new Season();
        season.setLeague(league);
        season.setCurrent(true);
        season.setNextRoundNumber(1);
        season = seasonRepository.save(season);

        league.setCurrentSeason(season);
        leagueRepository.save(league);

        // Setup Round
        Round round = new Round();
        round.setNumber(1);
        round.setSeason(season);
        round.setMatches(new ArrayList<>());
        round = roundRepository.save(round);
        season.getRounds().add(round);
        seasonRepository.save(season);

        // Setup Clubs and Teams
        List<Club> clubs = new ArrayList<>();
        int clubCount = 2;
        for (int i = 0; i < clubCount; i++) {
            Club club = new Club();
            club.setName("Club " + i);
            club.setLeague(league);
            club = clubRepository.save(club);

            Team team = new Team();
            team.setClub(club);

            // Stadium
            Stadium stadium = new Stadium("Stadium " + i);
            stadium.setClub(club);
            stadium.setCapacity(5000);
            stadium = stadiumRepository.save(stadium);
            club.setStadium(stadium);

            // Formation
            Formation formation = new Formation();
            formation.setModule(defaultModule);
            formation.setMentality(mentalityService.random());
            formation.setPlayers(new ArrayList<>());
            formation = formationRepository.save(formation);
            team.setFormation(formation);

            team = teamRepository.save(team);
            club.setTeam(team);
            clubRepository.save(club);
            clubs.add(club);

            // Create Players (11 for simplicity)
            List<Player> players = new ArrayList<>();
            for (int j = 0; j < 11; j++) {
                Player p = new Player();
                p.setName("Player " + i + "-" + j);
                p.setSurname("Surname " + i + "-" + j);
                p.setBirth(java.time.LocalDate.of(2000, 1, 1));
                p.setTeam(team);
                p.setStamina(100.0);
                p.setCondition(100.0);

                // Assign role based on position
                if (j == 0) p.setRole(PlayerRole.GOALKEEPER);
                else if (j < 5) p.setRole(PlayerRole.DEFENDER);
                else if (j < 9) p.setRole(PlayerRole.MIDFIELDER);
                else p.setRole(PlayerRole.FORWARD);

                // Stats
                p.setGoalkeeping(50.0);
                p.setDefending(50.0);
                p.setScoring(50.0);
                p.setWinger(50.0);
                p.setPassing(50.0);
                p.setPlaymaking(50.0);
                p.setSetPieces(50.0);
                p.setStamina(100.0);

                p = playerRepository.save(p);
                players.add(p);
                formation.addPlayer(p);
            }
            formationRepository.save(formation);
        }

        Match match = new Match();
        match.setHome(clubs.get(0));
        match.setAway(clubs.get(1));
        match.setHomeScore(0);
        match.setAwayScore(0);
        match.setRound(round);
        match.setDate(LocalDateTime.now());
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);
        Long matchId = match.getId();

        entityManager.flush();
        entityManager.clear();

        // Execution
        Optional<Match> loadedMatchOpt = matchRepository.findByIdWithSimulationData(matchId);

        // Verification
        Assertions.assertTrue(loadedMatchOpt.isPresent(), "Match should be found");
        Match loadedMatch = loadedMatchOpt.get();

        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getHome()), "Home club should be initialized");
        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getAway()), "Away club should be initialized");

        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getHome().getTeam()), "Home team should be initialized");
        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getAway().getTeam()), "Away team should be initialized");

        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getHome().getStadium()), "Home stadium should be initialized");

        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getRound()), "Round should be initialized");
        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getRound().getSeason()), "Season should be initialized");

        // Formations
        Assertions.assertTrue(Hibernate.isInitialized(loadedMatch.getHome().getTeam().getFormation()), "Home Team Formation should be initialized");

        logger.info("Successfully verified findByIdWithSimulationData fetches associations eagerly.");
    }
}
