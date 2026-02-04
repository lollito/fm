package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.EventType;
import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchEvent;
import com.lollito.fm.model.MatchPhase;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.LiveMatchSessionRepository;
import com.lollito.fm.repository.rest.MatchEventRepository;
import com.lollito.fm.repository.rest.MatchRepository;

@ExtendWith(MockitoExtension.class)
class LiveMatchServiceTest {

    @Mock
    private LiveMatchSessionRepository liveMatchSessionRepository;

    @Mock
    private MatchEventRepository matchEventRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MatchService matchService;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private LiveMatchService liveMatchService;

    @Test
    void testStartLiveMatch() {
        LiveMatchService spyService = Mockito.spy(liveMatchService);
        ReflectionTestUtils.setField(spyService, "self", spyService);
        doNothing().when(spyService).startMatchSimulation(any());

        Match match = createTestMatch();
        when(matchService.findById(1L)).thenReturn(match);
        when(liveMatchSessionRepository.save(any(LiveMatchSession.class))).thenAnswer(i -> i.getArguments()[0]);
        when(matchEventRepository.save(any(MatchEvent.class))).thenAnswer(i -> i.getArguments()[0]);

        LiveMatchSession session = spyService.startLiveMatch(1L);

        assertThat(session.getMatch()).isEqualTo(match);
        assertThat(session.getCurrentPhase()).isEqualTo(MatchPhase.PRE_MATCH);
        assertThat(session.getHomeScore()).isEqualTo(0);
        assertThat(session.getAwayScore()).isEqualTo(0);

        // Match status should be updated
        assertThat(match.getStatus()).isEqualTo(MatchStatus.LIVE);
        verify(matchRepository).save(match);
    }

    @Test
    void testEventCreation() {
        LiveMatchSession session = createTestSession();
        Team team = session.getMatch().getHome().getTeam();
        Player player = createTestPlayer();

        when(matchEventRepository.save(any(MatchEvent.class))).thenAnswer(i -> i.getArguments()[0]);

        MatchEvent event = liveMatchService.createMatchEvent(
            session, team, player, EventType.GOAL, 25, "Goal!", "Great goal!");

        assertThat(event.getEventType()).isEqualTo(EventType.GOAL);
        assertThat(event.getMinute()).isEqualTo(25);
        assertThat(event.getPlayer()).isEqualTo(player);
        assertThat(event.getSession()).isEqualTo(session);
    }

    @Test
    void testGoalProcessing() {
        LiveMatchSession session = createTestSession();
        Team homeTeam = session.getMatch().getHome().getTeam();
        MatchEvent goalEvent = createTestGoalEvent(session);

        when(matchEventRepository.save(any(MatchEvent.class))).thenAnswer(i -> i.getArguments()[0]);

        liveMatchService.processGoalEvent(session, goalEvent, homeTeam);

        assertThat(session.getHomeScore()).isEqualTo(1);
    }

    private Match createTestMatch() {
        Team homeTeam = new Team();
        homeTeam.setPlayers(new ArrayList<>());
        homeTeam.setId(1L);
        Club home = new Club();
        home.setTeam(homeTeam);
        home.setName("Home FC");

        Team awayTeam = new Team();
        awayTeam.setPlayers(new ArrayList<>());
        awayTeam.setId(2L);
        Club away = new Club();
        away.setTeam(awayTeam);
        away.setName("Away FC");

        return Match.builder()
                .id(1L)
                .home(home)
                .away(away)
                .status(MatchStatus.SCHEDULED)
                .build();
    }

    private LiveMatchSession createTestSession() {
        return LiveMatchSession.builder()
                .match(createTestMatch())
                .homeScore(0)
                .awayScore(0)
                .events(new ArrayList<>())
                .build();
    }

    private Player createTestPlayer() {
        Player player = new Player();
        player.setName("John");
        player.setSurname("Doe");
        return player;
    }

    private MatchEvent createTestGoalEvent(LiveMatchSession session) {
        return MatchEvent.builder()
                .match(session.getMatch())
                .player(createTestPlayer())
                .eventType(EventType.GOAL)
                .minute(10)
                .build();
    }
}
