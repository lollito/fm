package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Player;
import com.lollito.fm.repository.rest.PlayerRepository;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void getOffenceAverage_ShouldReturnZero_WhenListIsNull() {
        assertEquals(0, playerService.getOffenceAverage(null));
    }

    @Test
    void getOffenceAverage_ShouldReturnZero_WhenListIsEmpty() {
        assertEquals(0, playerService.getOffenceAverage(Collections.emptyList()));
    }

    @Test
    void getOffenceAverage_ShouldIgnoreNullPlayers_AndCalculateAverage() {
        Player p1 = mock(Player.class);
        when(p1.getOffenceAverage()).thenReturn(80);

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(null);

        assertEquals(80, playerService.getOffenceAverage(players));
    }

    @Test
    void getOffenceAverage_ShouldReturnAverage_WhenListHasValidPlayers() {
        Player p1 = mock(Player.class);
        when(p1.getOffenceAverage()).thenReturn(80);

        Player p2 = mock(Player.class);
        when(p2.getOffenceAverage()).thenReturn(60);

        List<Player> players = Arrays.asList(p1, p2);

        // (80 + 60) / 2 = 70
        assertEquals(70, playerService.getOffenceAverage(players));
    }

    @Test
    void getOffenceAverage_ShouldReturnZero_WhenListHasOnlyNullPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(null);
        players.add(null);

        assertEquals(0, playerService.getOffenceAverage(players));
    }
}
