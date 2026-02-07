package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.model.dto.StatsDTO;
import com.lollito.fm.repository.rest.LiveMatchSessionRepository;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.service.LiveMatchService.LiveMatchData;

@ExtendWith(MockitoExtension.class)
class LiveMatchServiceTest {

    @Mock
    private LiveMatchSessionRepository liveMatchSessionRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private MatchProcessor matchProcessor;

    @InjectMocks
    private LiveMatchService liveMatchService;

    private Match match;
    private MatchDTO matchDTO;
    private LiveMatchSession session;

    @BeforeEach
    void setUp() throws Exception {
        match = new Match();
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);
        Club home = new Club();
        home.setId(1L);
        match.setHome(home);
        Club away = new Club();
        away.setId(2L);
        match.setAway(away);

        matchDTO = new MatchDTO();
        matchDTO.setId(1L);
        matchDTO.setHomeScore(0);
        matchDTO.setAwayScore(0);
        matchDTO.setEvents(new ArrayList<>());
        matchDTO.setStats(new StatsDTO());
        matchDTO.setPlayerStats(new ArrayList<>());

        session = LiveMatchSession.builder()
                .matchId(1L)
                .startTime(LocalDateTime.now().minusSeconds(10))
                .currentMinute(0)
                .homeScore(0)
                .awayScore(0)
                .events("[]")
                .stats("{}")
                .playerStats("[]")
                .finished(false)
                .build();
    }

    @Test
    void testCreateSession() {
        when(matchMapper.toDto(any(Match.class))).thenReturn(matchDTO);
        when(liveMatchSessionRepository.save(any(LiveMatchSession.class))).thenReturn(session);

        liveMatchService.createSession(match);

        verify(liveMatchSessionRepository).save(any(LiveMatchSession.class));
        verify(matchMapper).toDto(match);
    }

    @Test
    void testUpdateLiveMatches() {
        List<LiveMatchSession> sessions = new ArrayList<>();
        sessions.add(session);
        when(liveMatchSessionRepository.findByFinishedFalse()).thenReturn(sessions);
        when(liveMatchSessionRepository.save(any(LiveMatchSession.class))).thenReturn(session);

        liveMatchService.updateLiveMatches();

        verify(liveMatchSessionRepository).save(any(LiveMatchSession.class));
        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void testGetLiveMatchData() throws Exception {
        when(liveMatchSessionRepository.findByMatchId(1L)).thenReturn(Optional.of(session));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchMapper.toDto(match)).thenReturn(matchDTO);

        Object data = liveMatchService.getLiveMatchData(1L);

        assertThat(data).isInstanceOf(LiveMatchData.class);
        LiveMatchData liveData = (LiveMatchData) data;
        assertThat(liveData.getMatch().getId()).isEqualTo(1L);
        assertThat(liveData.getCurrentMinute()).isEqualTo(0);
    }

    @Test
    void testForceFinish() {
        when(liveMatchSessionRepository.findByMatchId(1L)).thenReturn(Optional.of(session));

        liveMatchService.forceFinish(1L);

        assertThat(session.getFinished()).isTrue();
        assertThat(session.getCurrentMinute()).isEqualTo(90);
        verify(liveMatchSessionRepository).save(session);
        verify(matchProcessor).finalizeMatch(1L, session);
        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void testReset() {
        when(liveMatchSessionRepository.findByMatchId(1L)).thenReturn(Optional.of(session));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        // Setup match with some "dirty" state
        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setHomeScore(2);
        match.setAwayScore(1);
        match.setFinish(true);

        liveMatchService.reset(1L);

        verify(liveMatchSessionRepository).delete(session);
        verify(matchRepository).save(match);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(match.getHomeScore()).isNull();
        assertThat(match.getAwayScore()).isNull();
        assertThat(match.getFinish()).isFalse();
        assertThat(match.getStats()).isNotNull();
        assertThat(match.getEvents()).isEmpty();
        assertThat(match.getPlayerStats()).isEmpty();
    }
}
