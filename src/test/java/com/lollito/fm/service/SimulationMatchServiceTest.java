package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.dto.MatchResult;
import com.lollito.fm.model.Mentality;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
public class SimulationMatchServiceTest {

    @InjectMocks
    private SimulationMatchService simulationMatchService;

    @Mock private FormationService formationService;
    @Mock private PlayerService playerService;
    @Mock private MatchRepository matchRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private RankingService rankingService;
    @Mock private StadiumService stadiumService;
    @Mock private PlayerRepository playerRepository;
    @Mock private PlayerHistoryService playerHistoryService;
    @Mock private InjuryService injuryService;
    @Mock private AchievementService achievementService;
    @Mock private StaffService staffService;

    @Test
    public void testSimulateMatchesBatch() {
        // 1. Setup Data
        Match match1 = new Match();
        match1.setId(1L);
        Club homeClub1 = createClub("Home FC 1");
        Club awayClub1 = createClub("Away FC 1");
        match1.setHome(homeClub1);
        match1.setAway(awayClub1);
        match1.setHomeScore(0);
        match1.setAwayScore(0);

        Match match2 = new Match();
        match2.setId(2L);
        Club homeClub2 = createClub("Home FC 2");
        Club awayClub2 = createClub("Away FC 2");
        match2.setHome(homeClub2);
        match2.setAway(awayClub2);
        match2.setHomeScore(0);
        match2.setAwayScore(0);

        List<Match> matches = new ArrayList<>();
        matches.add(match1);
        matches.add(match2);

        // Pre-built formations
        Formation homeFormation1 = createFormation(homeClub1.getTeam().getPlayers());
        Formation awayFormation1 = createFormation(awayClub1.getTeam().getPlayers());
        Formation homeFormation2 = createFormation(homeClub2.getTeam().getPlayers());
        Formation awayFormation2 = createFormation(awayClub2.getTeam().getPlayers());

        // 2. Configure Mocks
        when(stadiumService.getCapacity(any())).thenReturn(50000);
        when(staffService.calculateClubStaffBonuses(any(Club.class))).thenReturn(new com.lollito.fm.dto.StaffBonusesDTO());

        // Mock formation creation for all
        when(formationService.createFormation(anyList(), any())).thenReturn(homeFormation1, awayFormation1, homeFormation2, awayFormation2);

        // Mock getting players by position
        when(formationService.getDefender(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(0, 4)));
        when(formationService.getMiedfileder(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(4, 8)));
        when(formationService.getOffender(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(8, 11)));

        // 3. Execute
        simulationMatchService.simulate(matches);

        // 4. Verify
        // Check if batch update was called once with list of all stats
        verify(playerHistoryService, times(1)).updateMatchStatisticsBatch(anyList());
        // Verify individual update was NOT called
        verify(playerHistoryService, times(0)).updateMatchStatistics(any(), any());

        verify(matchRepository).saveAll(matches);
        verify(rankingService).updateAll(matches);
    }

    @Test
    public void testSimulateMatch() {
        // 1. Setup Data
        Match match = new Match();
        match.setId(1L);

        Club homeClub = createClub("Home FC");
        Club awayClub = createClub("Away FC");

        match.setHome(homeClub);
        match.setAway(awayClub);
        match.setHomeScore(0);
        match.setAwayScore(0);

        // Pre-built formations
        Formation homeFormation = createFormation(homeClub.getTeam().getPlayers());
        Formation awayFormation = createFormation(awayClub.getTeam().getPlayers());

        // 2. Configure Mocks

        // Stadium
        when(stadiumService.getCapacity(any())).thenReturn(50000);
        when(staffService.calculateClubStaffBonuses(any(Club.class))).thenReturn(new com.lollito.fm.dto.StaffBonusesDTO());

        // Formations
        when(formationService.createFormation(anyList(), any())).thenReturn(homeFormation).thenReturn(awayFormation);

        // Player lists for positions (Simplified: just return all players for any position request)
        // In real logic, it filters by position, but for the test flow, returning a non-empty list is key.
        // However, the logic relies on removing players for cards/substitutions, so copies are better.
        // Or we can mock specific calls if we know the order.
        // Let's rely on the service logic calling these.

        // We need to return specific lists because the logic does:
        // homePlayers.put(PlayerPosition.DEFENCE, formationService.getDefender(homeFormation));
        // ...

        // We'll just return a subset for each to be safe, or the whole list.
        when(formationService.getDefender(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(0, 4)));
        when(formationService.getMiedfileder(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(4, 8)));
        when(formationService.getOffender(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(8, 11)));

        // Player Stats
        when(playerService.getOffenceAverage(anyList())).thenReturn(80);
        when(playerService.getDefenceAverage(anyList())).thenReturn(80);

        // Injury
        when(injuryService.checkForInjury(any(), anyDouble(), anyDouble())).thenReturn(false);

        // 3. Control Randomness
        try (MockedStatic<RandomUtils> mockedRandom = mockStatic(RandomUtils.class)) {
            // Default fallback for integers (e.g. spectators, luck, injury minute)
            mockedRandom.when(() -> RandomUtils.randomValue(anyInt(), anyInt())).thenReturn(30000);

            // Number of actions
            mockedRandom.when(() -> RandomUtils.randomValue(eq(15), eq(25))).thenReturn(20);

            // Coin toss (0 = Home Ball)
            mockedRandom.when(() -> RandomUtils.randomValue(eq(0), eq(1))).thenReturn(0);

            // Percentage checks (Pass completion, Goal chance, etc.) -> Always True for deterministic success
            mockedRandom.when(() -> RandomUtils.randomPercentage(anyDouble())).thenReturn(true);

            // Pick specific player from list (Always first)
            mockedRandom.when(() -> RandomUtils.randomValueFromList(anyList()))
                .thenAnswer(invocation -> {
                    List<?> list = invocation.getArgument(0);
                    if (list == null || list.isEmpty()) return null;
                    return list.get(0);
                });

            // 4. Execute
            MatchResult result = simulationMatchService.simulate(match);

            // 5. Verify
            assertNotNull(result);
            assertEquals("Home FC", result.getHomeTeam());
            assertEquals("Away FC", result.getAwayTeam());
            assertTrue(match.getFinish());

            // Verify interactions
            verify(matchRepository).save(match);
            verify(rankingService).update(match);
            verify(playerService).saveAll(anyList()); // Condition update saves players

            // Since we mocked randomPercentage to true, goals should happen.
            // Home starts with ball (Coin=0).
            // Action 1: Pass (true), Advance (true) -> Position increments (Midfield -> Offence)
            // Action 2: Pass (true), Goal (true) -> Home Goal. Ball to Away.
            // ...
            // With 20 actions and perfect success rate, we expect 10 goals total (5 each).
            assertEquals(5, match.getHomeScore());
            assertEquals(5, match.getAwayScore());
        }
    }

    private Club createClub(String name) {
        Club club = new Club();
        club.setName(name);
        club.setStadium(new Stadium());

        Team team = new Team();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Player p = new Player();
            p.setId((long) i + (name.hashCode())); // unique IDs
            p.setSurname(name + " Player " + i);
            p.setStamina(100.0);
            p.setGoalkeeping(50.0);
            p.setDefending(50.0);
            p.setScoring(50.0);
            p.setPassing(50.0);
            p.setPlaymaking(50.0);
            p.setWinger(50.0);
            p.setSetPieces(50.0);
            p.setCondition(100.0);
            players.add(p);
        }
        team.setPlayers(players);

        // Initial dummy formation
        Formation f = new Formation();
        f.setPlayers(players);
        team.setFormation(f);

        club.setTeam(team);
        return club;
    }

    private Formation createFormation(List<Player> players) {
        Formation f = new Formation();
        f.setPlayers(new ArrayList<>(players)); // Copy
        Module m = new Module();
        m.setCd(4);
        m.setWb(0);
        m.setMf(4);
        m.setWng(0);
        m.setFw(2);
        f.setModule(m);
        f.setMentality(Mentality.NORMAL);

        // Assign roles/positions implicitly by index for test
        // GK
        Player gk = players.get(0);
        // CD 1-4
        // MF 5-8
        // FW 9-10

        return f;
    }
}
