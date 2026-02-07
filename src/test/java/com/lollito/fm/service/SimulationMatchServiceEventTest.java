package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.MatchRepository;
import com.lollito.fm.repository.rest.PlayerRepository;

@ExtendWith(MockitoExtension.class)
public class SimulationMatchServiceEventTest {

    @InjectMocks
    private SimulationMatchService simulationMatchService;

    @Mock private FormationService formationService;
    @Mock private PlayerService playerService;
    @Mock private MatchRepository matchRepository;
    @Mock private StadiumService stadiumService;
    @Mock private PlayerRepository playerRepository;
    @Mock private PlayerHistoryService playerHistoryService;
    @Mock private InjuryService injuryService;
    @Mock private RankingService rankingService;

    private Match match;
    private Player playerWithNullCondition;

    @BeforeEach
    public void setup() {
        // Setup Players
        List<Player> homePlayers = createPlayers(11);
        List<Player> awayPlayers = createPlayers(11);

        Team homeTeam = new Team();
        homeTeam.setPlayers(homePlayers);
        Team awayTeam = new Team();
        awayTeam.setPlayers(awayPlayers);

        Club homeClub = new Club();
        homeClub.setName("Home FC");
        homeClub.setTeam(homeTeam);
        homeClub.setStadium(new Stadium());

        Club awayClub = new Club();
        awayClub.setName("Away FC");
        awayClub.setTeam(awayTeam);
        awayClub.setStadium(new Stadium());

        match = new Match();
        match.setHome(homeClub);
        match.setAway(awayClub);
        match.setHomeFormation(new Formation()); // will be overwritten by logic, but needed for struct?
        // Logic calls: match.getHome().getTeam().setFormation(...)

        // Mocks
        when(stadiumService.getCapacity(any())).thenReturn(10000);

        // Mock Formation Service to return valid formations
        when(formationService.createFormation(anyList(), any())).thenAnswer(invocation -> {
            List<Player> players = invocation.getArgument(0);
            Formation f = new Formation();
            f.setPlayers(players);
            f.setModule(new Module("4-4-2", 2, 2, 4, 0, 2));
            // Mock getters for positions
            return f;
        });

        // Mock Position getters in FormationService (used in simulateMatchLogic via helper?)
        // Actually SimulationMatchService calls: formationService.getDefender(formation) etc.
        // Return NEW ArrayLists to avoid ConcurrentModificationException during simulation logic which modifies these lists
        when(formationService.getDefender(any())).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(1, 5)));
        when(formationService.getMiedfileder(any())).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(5, 9)));
        when(formationService.getOffender(any())).thenAnswer(i -> new ArrayList<>(((Formation)i.getArgument(0)).getPlayers().subList(9, 11)));

        // Mock Player averages
        when(playerService.getOffenceAverage(anyList())).thenReturn(50);
        when(playerService.getDefenceAverage(anyList())).thenReturn(50);

        playerWithNullCondition = new Player("Null", "Condition", LocalDate.now());
        playerWithNullCondition.setCondition(null);
    }

    private List<Player> createPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Player p = new Player("Name" + i, "Surname" + i, LocalDate.now());
            p.setStamina(80.0);
            p.setScoring(50.0);
            p.setDefending(50.0);
            p.setGoalkeeping(50.0);
            p.setSetPieces(50.0);
            p.setPlaymaking(50.0);
            p.setPassing(50.0);
            p.setWinger(50.0);
            // Default condition 100.0
            p.setRole(i == 0 ? PlayerRole.GOALKEEPER : PlayerRole.MIDFIELDER);
            players.add(p);
        }
        return players;
    }

    @Test
    public void testEventPersistenceReference() {
        // Run simulation
        simulationMatchService.simulate(match);

        // Check if events were generated
        List<EventHistory> events = match.getEvents();
        assertFalse(events.isEmpty(), "Events should be generated");

        // Verify the BUG: Check if events have match reference set
        // Expecting this to FAIL currently if the bug exists
        for (EventHistory event : events) {
             assertNotNull(event.getMatch(), "EventHistory.match reference should not be null");
             assertEquals(match, event.getMatch(), "EventHistory.match should point to the match object");
        }
    }

    @Test
    public void testNullConditionNPE() {
        // Setup a player with NULL condition
        match.getHome().getTeam().getPlayers().get(0).setCondition(null);

        // Expect NPE currently if the bug exists.
        // If simulationMatchService.simulate is called, it eventually calls playMatch, then decrementCondition.
        // We catch the exception to verify it was thrown, or assertDoesNotThrow if we want to ensure it works.
        // Since we are reproducing the bug, we can try-catch or expect failure.
        // But the previous thought was "assertDoesNotThrow" which implies I expect it to PASS.
        // If I want to demonstrate it fails, I should expect an Exception.
        // But JUnit is better at showing "it failed".
        // So I will stick to assertDoesNotThrow. If it throws, the test FAILS (Reproducing the issue).

        assertDoesNotThrow(() -> simulationMatchService.simulate(match), "Should not throw NPE even if condition is null");
    }
}
