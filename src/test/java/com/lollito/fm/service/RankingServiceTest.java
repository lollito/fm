package com.lollito.fm.service;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.User;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Season;

@ExtendWith(MockitoExtension.class)
public class RankingServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RankingService rankingService;

    @Test
    public void testLoadWithUserNoClub() {
        // Mock user with no club
        User user = new User();
        user.setClub(null);
        when(userService.getLoggedUser()).thenReturn(user);

        // Execute
        List<Ranking> rankings = rankingService.load();

        // Verify
        assertNotNull(rankings);
        assertEquals(0, rankings.size());
    }

    @Test
    public void testLoadWithUserAndClub() {
        // Mock user with club
        User user = new User();
        Club club = new Club();
        League league = new League();
        Season season = new Season();
        Ranking ranking = new Ranking();

        season.setRankingLines(Collections.singletonList(ranking));
        league.setCurrentSeason(season);
        club.setLeague(league);
        user.setClub(club);

        when(userService.getLoggedUser()).thenReturn(user);

        // Execute
        List<Ranking> rankings = rankingService.load();

        // Verify
        assertNotNull(rankings);
        assertEquals(1, rankings.size());
    }
}
