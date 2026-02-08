package com.lollito.fm.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.lollito.fm.event.MatchFinishedEvent;
import com.lollito.fm.event.ScoutingCompletedEvent;
import com.lollito.fm.event.TrainingCompletedEvent;
import com.lollito.fm.event.TransferCompletedEvent;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.QuestType;
import com.lollito.fm.model.User;
import com.lollito.fm.service.QuestService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QuestEventListener {

    @Autowired
    private QuestService questService;

    @EventListener
    public void onMatchFinished(MatchFinishedEvent event) {
        Match match = event.getMatch();
        log.debug("Processing match finished event for match {}", match.getId());

        processMatchForUser(match.getHome().getUser(), match, true);
        processMatchForUser(match.getAway().getUser(), match, false);
    }

    private void processMatchForUser(User user, Match match, boolean isHome) {
        if (user == null) return;

        // Play Match
        questService.incrementProgress(user, QuestType.PLAY_MATCH, 1);

        int homeScore = match.getHomeScore() != null ? match.getHomeScore() : 0;
        int awayScore = match.getAwayScore() != null ? match.getAwayScore() : 0;
        int userScore = isHome ? homeScore : awayScore;
        int opponentScore = isHome ? awayScore : homeScore;

        // Win Match
        if (userScore > opponentScore) {
            questService.incrementProgress(user, QuestType.WIN_MATCH, 1);
        }

        // Clean Sheet
        if (opponentScore == 0) {
            questService.incrementProgress(user, QuestType.CLEAN_SHEET, 1);
        }

        // Score Goals
        if (userScore > 0) {
            questService.incrementProgress(user, QuestType.SCORE_GOALS, userScore);
        }
    }

    @EventListener
    public void onTrainingCompleted(TrainingCompletedEvent event) {
        if (event.getSession() == null || event.getSession().getTeam() == null
                || event.getSession().getTeam().getClub() == null) {
            return;
        }

        User user = event.getSession().getTeam().getClub().getUser();
        if (user != null) {
            log.debug("Processing training completed event for user {}", user.getId());
            questService.incrementProgress(user, QuestType.TRAIN_SESSION, 1);
        }
    }

    @EventListener
    public void onScoutingCompleted(ScoutingCompletedEvent event) {
        if (event.getAssignment() == null || event.getAssignment().getScout() == null
                || event.getAssignment().getScout().getClub() == null) {
            return;
        }

        User user = event.getAssignment().getScout().getClub().getUser();
        if (user != null) {
            log.debug("Processing scouting completed event for user {}", user.getId());
            questService.incrementProgress(user, QuestType.SCOUT_PLAYER, 1);
        }
    }

    @EventListener
    public void onTransferCompleted(TransferCompletedEvent event) {
        if (event.getBuyerClub() == null) return;

        User user = event.getBuyerClub().getUser();
        if (user != null) {
            log.debug("Processing transfer completed event for user {}", user.getId());
            questService.incrementProgress(user, QuestType.SIGN_PLAYER, 1);
        }
    }
}
