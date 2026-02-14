package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
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

    private Player activePlayer;
    private Player retiredPlayer;

    @BeforeEach
    void setUp() {
        activePlayer = new Player();
        activePlayer.setBirth(LocalDate.now().minusYears(20)); // Age 20
        activePlayer.setStamina(50.0);
        activePlayer.setPlaymaking(50.0);
        activePlayer.setScoring(50.0);
        activePlayer.setWinger(50.0);
        activePlayer.setGoalkeeping(50.0);
        activePlayer.setPassing(50.0);
        activePlayer.setDefending(50.0);
        activePlayer.setSetPieces(50.0);

        retiredPlayer = new Player();
        retiredPlayer.setBirth(LocalDate.now().minusYears(42)); // Age 42
        retiredPlayer.setStamina(50.0);
        retiredPlayer.setPlaymaking(50.0);
        retiredPlayer.setScoring(50.0);
        retiredPlayer.setWinger(50.0);
        retiredPlayer.setGoalkeeping(50.0);
        retiredPlayer.setPassing(50.0);
        retiredPlayer.setDefending(50.0);
        retiredPlayer.setSetPieces(50.0);
    }

    @Test
    void testUpdateSkills_ActivePlayer_UpdatesSkills() {
        Double initialStamina = activePlayer.getStamina();

        playerService.updateSkills(activePlayer);

        assertThat(activePlayer.getStamina()).isNotEqualTo(initialStamina);
    }

    @Test
    void testUpdateSkills_RetiredPlayer_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.updateSkills(retiredPlayer);
        });

        assertThat(exception.getMessage()).isEqualTo("Retired player");
    }
}
