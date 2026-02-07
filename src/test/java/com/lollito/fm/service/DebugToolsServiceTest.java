package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.model.DebugAction;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.ModifyPlayerStatsRequest;
import com.lollito.fm.repository.rest.DebugActionRepository;
import com.lollito.fm.repository.rest.PlayerRepository;

@ExtendWith(MockitoExtension.class)
public class DebugToolsServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DebugActionRepository debugActionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DebugToolsService debugToolsService;

    private User adminUser;
    private Player player;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUsername("admin");

        player = Player.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .birth(LocalDate.of(2000, 1, 1))
                .stamina(50.0)
                .playmaking(50.0)
                .scoring(50.0)
                .winger(50.0)
                .goalkeeping(50.0)
                .passing(50.0)
                .defending(50.0)
                .setPieces(50.0)
                .condition(100.0)
                .moral(100.0)
                .build();
    }

    @Test
    void modifyPlayerStats_shouldUpdateStatsCorrectly() throws JsonProcessingException {
        Map<String, Integer> statModifications = new HashMap<>();
        statModifications.put("stamina", 10);
        statModifications.put("passing", -5);
        statModifications.put("condition", -10);

        ModifyPlayerStatsRequest request = ModifyPlayerStatsRequest.builder()
                .playerIds(Collections.singletonList(1L))
                .statModifications(statModifications)
                .overallAdjustment(2)
                .build();

        when(playerRepository.findAllById(any())).thenReturn(Collections.singletonList(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(debugActionRepository.save(any(DebugAction.class))).thenAnswer(invocation -> {
            DebugAction action = invocation.getArgument(0);
            if (action.getId() == null) {
                action.setId(1L);
            }
            return action;
        });
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        debugToolsService.modifyPlayerStats(request, adminUser);

        // With overall first:
        // stamina: 50 -> 52. Then +10 -> 62.
        // passing: 50 -> 52. Then -5 -> 47.
        // condition: 100 -> 100 (capped). Then -10 -> 90.
        // scoring: 50 -> 52 (only overall)

        assertEquals(62.0, player.getStamina(), 0.1, "Stamina check");
        assertEquals(47.0, player.getPassing(), 0.1, "Passing check");
        assertEquals(90.0, player.getCondition(), 0.1, "Condition check");
        assertEquals(52.0, player.getScoring(), 0.1, "Scoring check");

        verify(playerRepository, times(1)).save(player);
    }
}
