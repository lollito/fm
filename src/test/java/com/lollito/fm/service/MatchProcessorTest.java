package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.mapper.MatchPlayerStatsMapper;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.SeasonRepository;
import com.lollito.fm.service.MatchProcessor.NotificationDTO;

@ExtendWith(MockitoExtension.class)
class MatchProcessorTest {

    @Mock private MatchRepository matchRepository;
    @Mock private SimulationMatchService simulationMatchService;
    @Mock private SeasonService seasonService;
    @Mock private LeagueService leagueService;
    @Mock private SeasonRepository seasonRepository;
    @Mock private LiveMatchService liveMatchService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ObjectMapper objectMapper;
    @Mock private MatchMapper matchMapper;
    @Mock private MatchPlayerStatsMapper matchPlayerStatsMapper;
    @Mock private PlayerService playerService;
    @Mock private PlayerHistoryService playerHistoryService;
    @Mock private RankingService rankingService;

    @InjectMocks
    private MatchProcessor matchProcessor;

    private Match match;
    private User homeUser;
    private User awayUser;

    @BeforeEach
    void setUp() {
        homeUser = new User();
        homeUser.setId(10L);
        homeUser.setUsername("homeUser");

        awayUser = new User();
        awayUser.setId(20L);
        awayUser.setUsername("awayUser");

        Club home = new Club();
        home.setId(1L);
        home.setName("Home FC");
        home.setUser(homeUser);

        Club away = new Club();
        away.setId(2L);
        away.setName("Away FC");
        away.setUser(awayUser);

        match = new Match();
        match.setId(100L);
        match.setHome(home);
        match.setAway(away);
        match.setStatus(MatchStatus.SCHEDULED);
    }

    @Test
    void testProcessMatchNotifiesUsers() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));

        matchProcessor.processMatch(100L);

        // Verify simulation call
        verify(simulationMatchService).simulate(eq(match), any(), eq(false));

        // Verify notifications sent to users via user-specific destination
        verify(messagingTemplate).convertAndSendToUser(
                eq("homeUser"),
                eq("/queue/notifications"),
                any(NotificationDTO.class));

        verify(messagingTemplate).convertAndSendToUser(
                eq("awayUser"),
                eq("/queue/notifications"),
                any(NotificationDTO.class));
    }
}
