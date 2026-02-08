package com.lollito.fm.listener;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.event.MatchFinishedEvent;
import com.lollito.fm.event.ScoutingCompletedEvent;
import com.lollito.fm.event.TrainingCompletedEvent;
import com.lollito.fm.event.TransferCompletedEvent;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.QuestType;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutingAssignment;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.User;
import com.lollito.fm.service.QuestService;

@ExtendWith(MockitoExtension.class)
public class QuestEventListenerTest {

    @Mock
    private QuestService questService;

    @InjectMocks
    private QuestEventListener questEventListener;

    private User homeUser;
    private User awayUser;
    private Match match;

    @BeforeEach
    void setUp() {
        homeUser = new User();
        homeUser.setId(1L);

        awayUser = new User();
        awayUser.setId(2L);

        Club homeClub = new Club();
        homeClub.setUser(homeUser);

        Club awayClub = new Club();
        awayClub.setUser(awayUser);

        Team homeTeam = new Team();
        homeTeam.setClub(homeClub);

        Team awayTeam = new Team();
        awayTeam.setClub(awayClub);
        homeClub.setTeam(homeTeam);
        awayClub.setTeam(awayTeam);

        match = new Match();
        match.setId(100L);
        match.setHome(homeClub);
        match.setAway(awayClub);
    }

    @Test
    void onMatchFinished_HomeWin() {
        match.setHomeScore(2);
        match.setAwayScore(0);

        MatchFinishedEvent event = new MatchFinishedEvent(this, match);
        questEventListener.onMatchFinished(event);

        // Home User: Play, Win, Clean Sheet, Score Goals
        verify(questService).incrementProgress(homeUser, QuestType.PLAY_MATCH, 1);
        verify(questService).incrementProgress(homeUser, QuestType.WIN_MATCH, 1);
        verify(questService).incrementProgress(homeUser, QuestType.CLEAN_SHEET, 1);
        verify(questService).incrementProgress(homeUser, QuestType.SCORE_GOALS, 2);

        // Away User: Play
        verify(questService).incrementProgress(awayUser, QuestType.PLAY_MATCH, 1);
        verify(questService, never()).incrementProgress(awayUser, QuestType.WIN_MATCH, 1);
        verify(questService, never()).incrementProgress(awayUser, QuestType.CLEAN_SHEET, 1);
        verify(questService, never()).incrementProgress(eq(awayUser), eq(QuestType.SCORE_GOALS), anyInt());
    }

    @Test
    void onTrainingCompleted() {
        TrainingSession session = new TrainingSession();
        Team team = match.getHome().getTeam();
        session.setTeam(team);

        TrainingCompletedEvent event = new TrainingCompletedEvent(this, session);
        questEventListener.onTrainingCompleted(event);

        verify(questService).incrementProgress(homeUser, QuestType.TRAIN_SESSION, 1);
    }

    @Test
    void onScoutingCompleted() {
        Scout scout = new Scout();
        Club club = match.getHome();
        scout.setClub(club);

        ScoutingAssignment assignment = new ScoutingAssignment();
        assignment.setScout(scout);

        ScoutingCompletedEvent event = new ScoutingCompletedEvent(this, assignment);
        questEventListener.onScoutingCompleted(event);

        verify(questService).incrementProgress(homeUser, QuestType.SCOUT_PLAYER, 1);
    }

    @Test
    void onTransferCompleted() {
        Player player = new Player();
        Club buyerClub = match.getHome();
        Club sellerClub = match.getAway();
        BigDecimal amount = BigDecimal.valueOf(1000000);

        TransferCompletedEvent event = new TransferCompletedEvent(this, player, buyerClub, sellerClub, amount);
        questEventListener.onTransferCompleted(event);

        verify(questService).incrementProgress(homeUser, QuestType.SIGN_PLAYER, 1);
    }
}
