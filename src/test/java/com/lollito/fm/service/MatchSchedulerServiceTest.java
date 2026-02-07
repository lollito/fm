package com.lollito.fm.service;

import com.lollito.fm.model.*;
import com.lollito.fm.repository.rest.LiveMatchSessionRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.repository.rest.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchSchedulerServiceTest {

    @Autowired private MatchSchedulerService matchSchedulerService;
    @Autowired private MatchRepository matchRepository;
    @Autowired private ServerService serverService;
    @Autowired private SeasonRepository seasonRepository;
    @Autowired private CountryService countryService;
    @Autowired private ModuleService moduleService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private MatchProcessor matchProcessor;
    @Autowired private LiveMatchSessionRepository liveMatchSessionRepository;

    @Test
    public void testProcessScheduledMatches() throws InterruptedException {
        // Auth as lollito (created by DatabaseLoader)
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("lollito", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create a server, which creates leagues, seasons, rounds, and matches
        Server server = serverService.create("TestGameAsync");
        League league = server.getLeagues().get(0);
        Season season = league.getCurrentSeason();
        Round round = season.getRounds().get(0);
        List<Match> matches = round.getMatches();

        // Set matches to be in the past
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        for (Match match : matches) {
            match.setDate(past);
            match.setStatus(MatchStatus.SCHEDULED);
        }
        matchRepository.saveAll(matches);

        // Run the scheduler
        matchSchedulerService.processScheduledMatches();

        // Wait for async processing and force finalization
        int attempts = 0;
        boolean allCompleted = false;
        while (attempts < 30 && !allCompleted) {
            Thread.sleep(1000);

            // Force finalize any created sessions to avoid waiting 3 minutes
            List<LiveMatchSession> sessions = liveMatchSessionRepository.findByFinishedFalse();
            System.out.println("Attempt " + attempts + ": Found " + sessions.size() + " active sessions.");

            for (LiveMatchSession session : sessions) {
                 if (matches.stream().anyMatch(m -> m.getId().equals(session.getMatchId()))) {
                     System.out.println("Finalizing match " + session.getMatchId());
                     session.setFinished(true);
                     session.setCurrentMinute(90);
                     liveMatchSessionRepository.save(session);
                     matchProcessor.finalizeMatch(session.getMatchId(), session);
                 }
            }

            List<Match> updatedMatches = matchRepository.findAllById(
                    matches.stream().map(Match::getId).collect(Collectors.toList())
            );
            allCompleted = updatedMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.COMPLETED);
            if (!allCompleted) {
                 long completedCount = updatedMatches.stream().filter(m -> m.getStatus() == MatchStatus.COMPLETED).count();
                 System.out.println("Completed matches: " + completedCount + "/" + matches.size());
            }
            attempts++;
        }

        // Verify matches are completed
        List<Match> updatedMatches = matchRepository.findAllById(
                matches.stream().map(Match::getId).collect(Collectors.toList())
        );
        for (Match match : updatedMatches) {
            assertEquals(MatchStatus.COMPLETED, match.getStatus(), "Match " + match.getId() + " should be COMPLETED");
            assertTrue(match.getFinish(), "Match " + match.getId() + " should be finished");
        }

        // Verify round progression
        Season updatedSeason = seasonRepository.findById(season.getId()).get();
        assertEquals(2, updatedSeason.getNextRoundNumber());
    }
}
