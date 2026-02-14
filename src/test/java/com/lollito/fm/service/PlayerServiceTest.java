package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.repository.rest.PlayerRepository;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void testCreateGk() {
        Player player = new Player();
        Player result = playerService.createGk(player);

        assertEquals(PlayerRole.GOALKEEPER, result.getRole());
        assertRange(result.getStamina(), 20.0, 90.0);
        assertRange(result.getPlaymaking(), 1.0, 50.0);
        assertRange(result.getScoring(), 1.0, 20.0);
        assertRange(result.getWinger(), 1.0, 20.0);
        assertRange(result.getGoalkeeping(), 45.0, 99.0);
        assertRange(result.getPassing(), 1.0, 20.0);
        assertRange(result.getDefending(), 30.0, 80.0);
        assertRange(result.getSetPieces(), 40.0, 99.0);

        assertNotNull(result.getPotential());
        assertTrue(result.getPotential() <= 99.0);
    }

    @Test
    void testCreateCd() {
        Player player = new Player();
        Player result = playerService.createCd(player);

        assertEquals(PlayerRole.DEFENDER, result.getRole());
        assertRange(result.getStamina(), 20.0, 90.0);
        assertRange(result.getPlaymaking(), 20.0, 70.0);
        assertRange(result.getScoring(), 20.0, 50.0);
        assertRange(result.getWinger(), 1.0, 20.0);
        assertRange(result.getGoalkeeping(), 1.0, 20.0);
        assertRange(result.getPassing(), 20.0, 70.0);
        assertRange(result.getDefending(), 40.0, 99.0);
        assertRange(result.getSetPieces(), 20.0, 80.0);

        assertNotNull(result.getPotential());
        assertTrue(result.getPotential() <= 99.0);
    }

    @Test
    void testCreateWb() {
        Player player = new Player();
        Player result = playerService.createWb(player);

        assertEquals(PlayerRole.WINGBACK, result.getRole());
        assertRange(result.getStamina(), 20.0, 90.0);
        assertRange(result.getPlaymaking(), 30.0, 70.0);
        assertRange(result.getScoring(), 20.0, 60.0);
        assertRange(result.getWinger(), 30.0, 60.0);
        assertRange(result.getGoalkeeping(), 1.0, 20.0);
        assertRange(result.getPassing(), 30.0, 80.0);
        assertRange(result.getDefending(), 40.0, 80.0);
        assertRange(result.getSetPieces(), 20.0, 99.0);

        assertNotNull(result.getPotential());
        assertTrue(result.getPotential() <= 99.0);
    }

    @Test
    void testCreateMf() {
        Player player = new Player();
        Player result = playerService.createMf(player);

        assertEquals(PlayerRole.MIDFIELDER, result.getRole());
        assertRange(result.getStamina(), 20.0, 90.0);
        assertRange(result.getPlaymaking(), 40.0, 99.0);
        assertRange(result.getScoring(), 30.0, 70.0);
        assertRange(result.getWinger(), 20.0, 50.0);
        assertRange(result.getGoalkeeping(), 1.0, 20.0);
        assertRange(result.getPassing(), 30.0, 80.0);
        assertRange(result.getDefending(), 35.0, 80.0);
        assertRange(result.getSetPieces(), 40.0, 99.0);

        assertNotNull(result.getPotential());
        assertTrue(result.getPotential() <= 99.0);
    }

    @Test
    void testCreateWng() {
        Player player = new Player();
        Player result = playerService.createWng(player);

        assertEquals(PlayerRole.WING, result.getRole());
        assertRange(result.getStamina(), 20.0, 90.0);
        assertRange(result.getPlaymaking(), 40.0, 80.0);
        assertRange(result.getScoring(), 30.0, 80.0);
        assertRange(result.getWinger(), 40.0, 99.0);
        assertRange(result.getGoalkeeping(), 1.0, 20.0);
        assertRange(result.getPassing(), 40.0, 99.0);
        assertRange(result.getDefending(), 20.0, 60.0);
        assertRange(result.getSetPieces(), 40.0, 99.0);

        assertNotNull(result.getPotential());
        assertTrue(result.getPotential() <= 99.0);
    }

    @Test
    void testCreateFw() {
        Player player = new Player();
        Player result = playerService.createFw(player);

        assertEquals(PlayerRole.FORWARD, result.getRole());
        assertRange(result.getStamina(), 20.0, 90.0);
        assertRange(result.getPlaymaking(), 20.0, 30.0);
        assertRange(result.getScoring(), 50.0, 99.0);
        assertRange(result.getWinger(), 20.0, 60.0);
        assertRange(result.getGoalkeeping(), 1.0, 20.0);
        assertRange(result.getPassing(), 30.0, 60.0);
        assertRange(result.getDefending(), 10.0, 50.0);
        assertRange(result.getSetPieces(), 40.0, 99.0);

        assertNotNull(result.getPotential());
        assertTrue(result.getPotential() <= 99.0);
    }

    private void assertRange(Double value, Double min, Double max) {
        assertNotNull(value, "Value should not be null");
        assertTrue(value >= min, "Value " + value + " is less than min " + min);
        // RandomUtils.randomValue(min, max) -> nextDouble(min, max + 1.0)
        // So value can be max.999... which is < max + 1.0
        assertTrue(value < max + 1.0, "Value " + value + " is greater than or equal to max bound " + (max + 1.0));
    }
}
