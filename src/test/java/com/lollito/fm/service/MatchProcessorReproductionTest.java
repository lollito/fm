package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.User;
import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.RoundRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.LiveMatchSessionRepository;
import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.TeamRepository;
import com.lollito.fm.repository.rest.StadiumRepository;
import java.time.LocalDate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@WithMockUser(username = "testuser")
public class MatchProcessorReproductionTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired MatchProcessor matchProcessor;
    @Autowired MatchRepository matchRepository;
    @Autowired ClubRepository clubRepository;
    @Autowired SeasonRepository seasonRepository;
    @Autowired RoundRepository roundRepository;
    @Autowired UserRepository userRepository;
    @Autowired CountryRepository countryRepository;
    @Autowired LeagueRepository leagueRepository;
    @Autowired EntityManager entityManager;
    @Autowired LiveMatchSessionRepository liveMatchSessionRepository;
    @Autowired ModuleRepository moduleRepository;
    @Autowired PlayerRepository playerRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired StadiumRepository stadiumRepository;
    @Autowired TransactionTemplate transactionTemplate;

    @MockBean SimpMessagingTemplate messagingTemplate;

    @Test
    public void testFinalizeMatchOptimisticLockingFailure() throws Exception {
        // Setup data
        Country country = new Country();
        country.setName("Italy");
        country = countryRepository.save(country);

        League league = new League();
        league.setName("Serie A");
        league.setCountry(country);
        league = leagueRepository.save(league);

        Season season = new Season();
        season.setLeague(league);
        season.setCurrent(true);
        season.setNextRoundNumber(1);
        season = seasonRepository.save(season);

        league.setCurrentSeason(season);
        leagueRepository.save(league);

        Round round = new Round();
        round.setNumber(1);
        round.setSeason(season);
        round.setMatches(new ArrayList<>());
        round = roundRepository.save(round);

        // Module
        Module module = new Module();
        module.setName("4-4-2");
        module.setCd(2);
        module.setWb(2);
        module.setMf(4);
        module.setWng(0);
        module.setFw(2);
        module = moduleRepository.save(module);

        // Clubs & Teams
        Club homeClub = createClubWithTeam(league, "Home Club", module);
        Club awayClub = createClubWithTeam(league, "Away Club", module);

        // Match
        Match match = new Match();
        match.setHome(homeClub);
        match.setAway(awayClub);
        match.setHomeScore(0);
        match.setAwayScore(0);
        match.setRound(round);
        match.setDate(LocalDateTime.now().plusDays(1));
        match.setStatus(MatchStatus.SCHEDULED);
        match = matchRepository.save(match);

        Long matchId = match.getId();

        // 1. Process Match (Async)
        // Since it's async, we call it and wait for status change or session creation
        matchProcessor.processMatch(matchId);

        // Wait for session to be created
        LiveMatchSession session = null;
        for (int i = 0; i < 20; i++) {
            Optional<LiveMatchSession> sessionOpt = liveMatchSessionRepository.findByMatchId(matchId);
            if (sessionOpt.isPresent()) {
                session = sessionOpt.get();
                break;
            }
            Thread.sleep(500);
        }
        Assertions.assertNotNull(session, "Session should be created");

        // Wait for match to be updated to IN_PROGRESS and events cleared
        // This confirms processMatch has finished its work
        for (int i = 0; i < 20; i++) {
            Match m = matchRepository.findById(matchId).get();
            if (m.getStatus() == MatchStatus.IN_PROGRESS) {
                // Ensure events are empty
                 // Events are lazy loaded, so we might need transaction if accessing them
                 // But repository findById returns a proxy or entity.
                 // We can check event count in DB
                break;
            }
            Thread.sleep(500);
        }

        transactionTemplate.execute(status -> {
            Match updatedMatch = matchRepository.findById(matchId).get();
            Assertions.assertEquals(MatchStatus.IN_PROGRESS, updatedMatch.getStatus());
            Assertions.assertTrue(updatedMatch.getEvents().isEmpty(), "Events should be empty after processMatch");
            return null;
        });

        // 2. Finalize Match
        // This is the step that fails with OptimisticLockingFailureException
        try {
            matchProcessor.finalizeMatch(matchId, session);
        } catch (Exception e) {
            // We expect an exception or logged error.
            // MatchProcessor.finalizeMatch catches Exception and logs error.
            // So we might not catch it here unless we check logs or check if match failed to finalize.
            // However, the user reported "Error finalizing match 91 ... ObjectOptimisticLockingFailureException".
            // If MatchProcessor catches it, the match status won't be COMPLETED?
            // Wait, MatchProcessor.finalizeMatch:
            // catch (Exception e) { logger.error(...); }
            // So it swallows the exception.
        }

        // Verify result
        Match finalMatch = matchRepository.findById(matchId).get();
        Assertions.assertEquals(MatchStatus.COMPLETED, finalMatch.getStatus(), "Match should be COMPLETED");
    }

    private Club createClubWithTeam(League league, String name, Module module) {
        Club club = new Club();
        club.setName(name);
        club.setLeague(league);

        Stadium stadium = new Stadium();
        stadium.setName(name + " Stadium");
        stadium.setCapacity(10000);
        club.setStadium(stadium);

        Team team = new Team();
        team.setFormation(new Formation());
        team.getFormation().setModule(module);

        club.setTeam(team);

        // Players
        List<Player> players = new ArrayList<>();
        for(int i=0; i<11; i++) {
            Player p = new Player();
            p.setName("Player " + i);
            p.setSurname("Surname " + i);
            p.setBirth(LocalDate.now().minusYears(20));
            p.setTeam(team);
            p.setRole(PlayerRole.MIDFIELDER);
            p.setStamina(80.0);
            p.setPlaymaking(50.0);
            p.setScoring(50.0);
            p.setWinger(50.0);
            p.setGoalkeeping(10.0);
            p.setPassing(50.0);
            p.setDefending(50.0);
            p.setSetPieces(50.0);
            if(i==0) p.setGoalkeeping(80.0); // Make first player a GK
            players.add(p);
        }
        team.setPlayers(players);
        team.getFormation().setPlayers(new ArrayList<>(players));

        club = clubRepository.save(club);
        return club;
    }
}
