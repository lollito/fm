package com.lollito.fm.mapper;

import com.lollito.fm.model.*;
import com.lollito.fm.model.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchMapperTest {

    @Autowired
    private MatchMapper matchMapper;

    @Test
    public void testToDto_WithFormationsAndStats() {
        // Setup Match
        Match match = new Match();
        match.setId(1L);

        Round round = new Round();
        round.setNumber(1);
        match.setRound(round);

        // Setup Formations
        Formation homeFormation = new Formation();
        homeFormation.setId(10L);
        match.setHomeFormation(homeFormation);

        Formation awayFormation = new Formation();
        awayFormation.setId(20L);
        match.setAwayFormation(awayFormation);

        // Setup Player Stats
        List<MatchPlayerStats> statsList = new ArrayList<>();
        MatchPlayerStats stats = new MatchPlayerStats();
        stats.setId(100L);
        stats.setGoals(2);
        stats.setRating(8.5);

        Player player = new Player();
        player.setId(50L);
        player.setName("John");
        player.setSurname("Doe");
        player.setBirth(java.time.LocalDate.of(2000, 1, 1));
        stats.setPlayer(player);

        statsList.add(stats);
        match.setPlayerStats(statsList);

        // Execute mapping
        MatchDTO dto = matchMapper.toDto(match);

        // Verify
        assertNotNull(dto);
        assertNotNull(dto.getHomeFormation());
        assertEquals(10L, dto.getHomeFormation().getId());

        assertNotNull(dto.getAwayFormation());
        assertEquals(20L, dto.getAwayFormation().getId());

        assertNotNull(dto.getPlayerStats());
        assertEquals(1, dto.getPlayerStats().size());
        MatchPlayerStatsDTO statsDto = dto.getPlayerStats().get(0);
        assertEquals(100L, statsDto.getId());
        assertEquals(2, statsDto.getGoals());
        assertEquals(8.5, statsDto.getRating());

        assertNotNull(statsDto.getPlayer());
        assertEquals(50L, statsDto.getPlayer().getId());
        assertEquals("John", statsDto.getPlayer().getName());
        assertEquals("Doe", statsDto.getPlayer().getSurname());
    }
}
