package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.MatchRepository;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchService matchService;

    private User user;
    private Club club;
    private League league;
    private Season season;
    private Round round;
    private List<Round> rounds;

    @BeforeEach
    void setUp() {
        // Construct the object graph
        round = new Round(1);
        round.setId(100L);

        rounds = new ArrayList<>();
        rounds.add(round); // Index 0

        season = new Season();
        season.setRounds(rounds);
        // default nextRoundNumber is 1, so loadNext -> loadByRoundNumber(0) -> accesses rounds.get(0)

        league = new League();
        league.setCurrentSeason(season);

        club = new Club();
        club.setLeague(league);

        user = new User();
        user.setClub(club);
    }

    @Test
    void loadNext_ShouldReturnMatches_WhenRoundExists() {
        // Arrange
        season.setNextRoundNumber(1);

        Match match1 = new Match();
        match1.setId(1L);
        List<Match> expectedMatches = Collections.singletonList(match1);

        when(userService.getLoggedUser()).thenReturn(user);
        when(matchRepository.findByRoundIdWithClubs(round.getId())).thenReturn(expectedMatches);

        // Act
        List<Match> actualMatches = matchService.loadNext();

        // Assert
        assertThat(actualMatches).isEqualTo(expectedMatches);
        verify(matchRepository).findByRoundIdWithClubs(round.getId());
    }

    @Test
    void loadNext_ShouldReturnEmptyList_WhenRoundIndexOutOfBounds() {
        // Arrange
        // If nextRoundNumber is 2, loadNext tries to access index 1 (2-1).
        // rounds only has 1 element (index 0). So size (1) <= number (1) is true.
        season.setNextRoundNumber(2);

        when(userService.getLoggedUser()).thenReturn(user);

        // Act
        List<Match> actualMatches = matchService.loadNext();

        // Assert
        assertThat(actualMatches).isEmpty();
        verifyNoInteractions(matchRepository);
    @Mock
    private Page<Match> matchPage;

    @Test
    public void loadHistory_shouldReturnPageOfMatches() {
        // Arrange
        User user = new User();
        Club club = new Club();
        club.setId(1L);
        user.setClub(club);

        when(userService.getLoggedUser()).thenReturn(user);

        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.findByClubAndFinishOrderByDateDesc(club, pageable)).thenReturn(matchPage);

        // Act
        Page<Match> result = matchService.loadHistory(pageable);

        // Assert
        assertEquals(matchPage, result);
        verify(userService).getLoggedUser();
        verify(matchRepository).findByClubAndFinishOrderByDateDesc(club, pageable);
    }
}
