package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
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
import com.lollito.fm.model.Mentality;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
public class SimulationMatchServiceHomeAdvantageTest {

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

    @Test
    public void testHomeAdvantage_FullStadium() {
        testHomeAdvantage(50000, 50000, 30);
    }

    @Test
    public void testHomeAdvantage_EmptyStadium() {
        testHomeAdvantage(50000, 0, 20);
    }

    private void testHomeAdvantage(int capacity, int spectators, int expectedMaxLuck) {
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
        when(stadiumService.getCapacity(any())).thenReturn(capacity);
        when(formationService.createFormation(anyList(), any())).thenReturn(homeFormation).thenReturn(awayFormation);
        when(formationService.getDefender(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(0, 4)));
        when(formationService.getMiedfileder(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(4, 8)));
        when(formationService.getOffender(any(Formation.class))).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(8, 11)));
        when(playerService.getOffenceAverage(anyList())).thenReturn(80);
        when(playerService.getDefenceAverage(anyList())).thenReturn(80);
        when(injuryService.checkForInjury(any(), anyDouble())).thenReturn(false);

        // 3. Control Randomness
        try (MockedStatic<RandomUtils> mockedRandom = mockStatic(RandomUtils.class)) {

            mockedRandom.when(() -> RandomUtils.randomValue(anyInt(), anyInt())).thenAnswer(invocation -> {
                int min = invocation.getArgument(0);
                int max = invocation.getArgument(1);
                // Identifying the spectators call
                if (max == capacity) {
                    return spectators;
                }
                // Default fallback for other randomValue(int, int) calls (like luck)
                return min; // Return minimum to be safe/deterministic
            });

            // Number of actions
            mockedRandom.when(() -> RandomUtils.randomValue(eq(15), eq(25))).thenReturn(1); // just 1 action to keep it short

            // Coin toss (0 = Home Ball)
            mockedRandom.when(() -> RandomUtils.randomValue(eq(0), eq(1))).thenReturn(0);

            // Mock randomPercentage to avoid NPEs or unwanted behavior
            mockedRandom.when(() -> RandomUtils.randomPercentage(anyDouble())).thenReturn(true);

            // Mock list selection
             mockedRandom.when(() -> RandomUtils.randomValueFromList(anyList()))
                .thenAnswer(invocation -> {
                    List<?> list = invocation.getArgument(0);
                    if (list == null || list.isEmpty()) return null;
                    return list.get(0);
                });

            // 4. Execute
            simulationMatchService.simulate(match);

            // 5. Verify
            // We expect RandomUtils.randomValue(0, expectedMaxLuck) to be called inside playMatch
            mockedRandom.verify(() -> RandomUtils.randomValue(0, expectedMaxLuck));
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
            p.setId((long) i + (name.hashCode()));
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
        Formation f = new Formation();
        f.setPlayers(players);
        team.setFormation(f);
        club.setTeam(team);
        return club;
    }

    private Formation createFormation(List<Player> players) {
        Formation f = new Formation();
        f.setPlayers(new ArrayList<>(players));
        Module m = new Module();
        m.setCd(4);
        m.setWb(0);
        m.setMf(4);
        m.setWng(0);
        m.setFw(2);
        f.setModule(m);
        f.setMentality(Mentality.NORMAL);
        return f;
    }
}
