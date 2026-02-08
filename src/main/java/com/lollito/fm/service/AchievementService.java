package com.lollito.fm.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.AchievementType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserAchievement;
import com.lollito.fm.repository.UserAchievementRepository;

@Service
public class AchievementService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private ManagerProgressionService managerProgressionService;

    // FinancialService might be needed if we need to fetch finance, but we can pass Finance object
    @Autowired
    private FinancialService financialService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void checkAndUnlock(User user, AchievementType achievement) {
        if (user == null || achievement == null) {
            return;
        }

        if (userAchievementRepository.existsByUserIdAndAchievement(user.getId(), achievement)) {
            return;
        }

        UserAchievement userAchievement = UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .build();

        userAchievementRepository.save(userAchievement);

        managerProgressionService.addXp(user, achievement.getXpReward());

        logger.info("Achievement Unlocked: {} for User {}", achievement.getName(), user.getUsername());
        notificationService.createNotification(user, com.lollito.fm.model.NotificationType.ACHIEVEMENT_UNLOCKED, "Achievement Unlocked", "You unlocked: " + achievement.getName(), com.lollito.fm.model.NotificationPriority.HIGH);
    }

    @Transactional
    public void checkMatchAchievements(Match match) {
        if (match == null || match.getHome() == null || match.getAway() == null) {
            return;
        }

        checkGoleador(match);
        checkComebackKing(match);
    }

    private void checkGoleador(Match match) {
        // Goleador: Win by 5 goals or more (or score >= 5 depending on interpretation, sticking to score >= 5 as per task)
        // Task says: Check match.getScoreHome() >= 5 -> checkAndUnlock(user, GOLEADOR)

        if (match.getHomeScore() >= 5) {
            User user = match.getHome().getUser();
            if (user != null) {
                checkAndUnlock(user, AchievementType.GOLEADOR);
            }
        }

        if (match.getAwayScore() >= 5) {
            User user = match.getAway().getUser();
            if (user != null) {
                checkAndUnlock(user, AchievementType.GOLEADOR);
            }
        }
    }

    private void checkComebackKing(Match match) {
        // Comeback King: Win a match after being 0-2 down

        User homeUser = match.getHome().getUser();
        User awayUser = match.getAway().getUser();

        if (homeUser == null && awayUser == null) {
            return;
        }

        boolean homeWon = match.getHomeScore() > match.getAwayScore();
        boolean awayWon = match.getAwayScore() > match.getHomeScore();

        if (!homeWon && !awayWon) return; // Draw

        List<EventHistory> events = match.getEvents();
        if (events == null || events.isEmpty()) return;

        // Replay score history
        // Note: EventHistory stores current score at that event? No, it has homeScore and awayScore fields sometimes.
        // Let's check EventHistory model or usage in SimulationMatchService.
        // In SimulationMatchService: events.add(new EventHistory(..., homeScore, awayScore));
        // So we can track score progression.

        boolean homeWasDownByTwo = false;
        boolean awayWasDownByTwo = false;

        // Sort events by minute just in case
        events.sort(Comparator.comparingInt(EventHistory::getMinute));

        for (EventHistory event : events) {
            if (event.getHomeScore() != null && event.getAwayScore() != null) {
                int scoreDiff = event.getHomeScore() - event.getAwayScore();

                if (scoreDiff <= -2) {
                    homeWasDownByTwo = true;
                }
                if (scoreDiff >= 2) {
                    awayWasDownByTwo = true;
                }
            }
        }

        if (homeWon && homeWasDownByTwo && homeUser != null) {
            checkAndUnlock(homeUser, AchievementType.COMEBACK_KING);
        }

        if (awayWon && awayWasDownByTwo && awayUser != null) {
            checkAndUnlock(awayUser, AchievementType.COMEBACK_KING);
        }
    }

    @Transactional
    public void checkSeasonAchievements(Season season) {
        if (season == null) return;

        List<Ranking> rankings = season.getRankingLines();
        if (rankings == null || rankings.isEmpty()) return;

        // Sort rankings
        List<Ranking> sortedRankings = rankings.stream()
            .sorted(Comparator.comparing(Ranking::getPoints).reversed()
                .thenComparing(Comparator.comparingInt((Ranking r) -> r.getGoalsFor() - r.getGoalAgainst()).reversed()) // Goal Diff
                .thenComparing(Comparator.comparing(Ranking::getGoalsFor).reversed())) // Goals For
            .collect(Collectors.toList());

        for (int i = 0; i < sortedRankings.size(); i++) {
            Ranking ranking = sortedRankings.get(i);
            Club club = ranking.getClub();
            User user = club.getUser();

            if (user != null) {
                // Win League
                if (i == 0) {
                    checkAndUnlock(user, AchievementType.WIN_LEAGUE);
                }

                // Promotion
                Integer promotionCount = season.getLeague().getPromotion();
                if (promotionCount != null && i < promotionCount) {
                     checkAndUnlock(user, AchievementType.PROMOTION);
                }
            }
        }
    }

    @Transactional
    public void checkFinancialAchievements(Finance finance) {
        if (finance == null || finance.getClub() == null || finance.getClub().getUser() == null) {
            return;
        }

        User user = finance.getClub().getUser();
        BigDecimal balance = finance.getBalance();

        // Tycoon: Accumulate a balance of 100M
        if (balance.compareTo(BigDecimal.valueOf(100_000_000)) >= 0) {
            checkAndUnlock(user, AchievementType.TYCOON);
        }
    }

    // Additional Helper for direct User check
    public void checkTycoon(User user) {
        if(user.getClub() != null && user.getClub().getFinance() != null) {
            checkFinancialAchievements(user.getClub().getFinance());
        }
    }
}
