package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.AchievementType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.League;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserAchievement;
import com.lollito.fm.repository.UserAchievementRepository;

@ExtendWith(MockitoExtension.class)
public class AchievementServiceTest {

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private ManagerProgressionService managerProgressionService;

    @Mock
    private FinancialService financialService;

    @InjectMocks
    private AchievementService achievementService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
    }

    @Test
    void checkAndUnlock_NewAchievement_ShouldUnlock() {
        AchievementType achievement = AchievementType.GOLEADOR;
        when(userAchievementRepository.existsByUserIdAndAchievement(user.getId(), achievement)).thenReturn(false);

        achievementService.checkAndUnlock(user, achievement);

        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
        verify(managerProgressionService, times(1)).addXp(eq(user), eq((long) achievement.getXpReward()));
    }

    @Test
    void checkAndUnlock_ExistingAchievement_ShouldNotUnlock() {
        AchievementType achievement = AchievementType.GOLEADOR;
        when(userAchievementRepository.existsByUserIdAndAchievement(user.getId(), achievement)).thenReturn(true);

        achievementService.checkAndUnlock(user, achievement);

        verify(userAchievementRepository, never()).save(any(UserAchievement.class));
        verify(managerProgressionService, never()).addXp(any(), any(Long.class));
    }

    @Test
    void checkMatchAchievements_Goleador_HomeWin() {
        Match match = new Match();
        match.setHomeScore(5);
        match.setAwayScore(0);

        Club homeClub = new Club();
        homeClub.setUser(user);
        match.setHome(homeClub);

        Club awayClub = new Club();
        match.setAway(awayClub);

        when(userAchievementRepository.existsByUserIdAndAchievement(user.getId(), AchievementType.GOLEADOR)).thenReturn(false);

        achievementService.checkMatchAchievements(match);

        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
    }

    @Test
    void checkMatchAchievements_ComebackKing_HomeWin() {
        Match match = new Match();
        match.setHomeScore(3);
        match.setAwayScore(2);

        Club homeClub = new Club();
        homeClub.setUser(user);
        match.setHome(homeClub);

        Club awayClub = new Club();
        match.setAway(awayClub);

        List<EventHistory> events = new ArrayList<>();
        // 0-1
        events.add(EventHistory.builder().homeScore(0).awayScore(1).minute(10).build());
        // 0-2 (Down by 2)
        events.add(EventHistory.builder().homeScore(0).awayScore(2).minute(20).build());
        // 1-2
        events.add(EventHistory.builder().homeScore(1).awayScore(2).minute(50).build());
        // 2-2
        events.add(EventHistory.builder().homeScore(2).awayScore(2).minute(70).build());
        // 3-2
        events.add(EventHistory.builder().homeScore(3).awayScore(2).minute(85).build());

        match.setEvents(events);

        when(userAchievementRepository.existsByUserIdAndAchievement(user.getId(), AchievementType.COMEBACK_KING)).thenReturn(false);

        achievementService.checkMatchAchievements(match);

        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
    }

    @Test
    void checkFinancialAchievements_Tycoon_ShouldUnlock() {
        Finance finance = new Finance();
        finance.setBalance(new BigDecimal("100000000")); // 100M

        Club club = new Club();
        club.setUser(user);
        finance.setClub(club);
        // We need to set club on finance as well because checkFinancialAchievements uses finance.getClub()
        // But finance doesn't store club directly in entity (mappedBy), but getter should exist or we mock.
        // Wait, Finance entity has `private Club club` with `@OneToOne(mappedBy...)`.
        // JPA populates it, but in test we need to set it.
        // Finance entity doesn't have `setClub` in Lombok builder if not included?
        // It has `@Setter`.
        finance.setClub(club);

        when(userAchievementRepository.existsByUserIdAndAchievement(user.getId(), AchievementType.TYCOON)).thenReturn(false);

        achievementService.checkFinancialAchievements(finance);

        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
    }

    @Test
    void checkSeasonAchievements_WinLeague() {
        Season season = new Season();
        List<Ranking> rankings = new ArrayList<>();

        Club userClub = new Club();
        userClub.setUser(user);

        Ranking r1 = new Ranking();
        r1.setClub(userClub);
        r1.setPoints(90);

        Ranking r2 = new Ranking();
        r2.setClub(new Club());
        r2.setPoints(80);

        rankings.add(r1);
        rankings.add(r2);
        season.setRankingLines(rankings);

        // Mock League for promotion check
        League league = new League();
        league.setPromotion(0); // No promotion to avoid confusing test
        season.setLeague(league);

        when(userAchievementRepository.existsByUserIdAndAchievement(user.getId(), AchievementType.WIN_LEAGUE)).thenReturn(false);

        achievementService.checkSeasonAchievements(season);

        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
    }
}
