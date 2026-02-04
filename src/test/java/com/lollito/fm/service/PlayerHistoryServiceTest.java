package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.PlayerAchievementRepository;
import com.lollito.fm.repository.rest.PlayerCareerStatsRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.PlayerSeasonStatsRepository;
import com.lollito.fm.repository.rest.PlayerTransferHistoryRepository;

@ExtendWith(MockitoExtension.class)
public class PlayerHistoryServiceTest {

    @Mock
    private PlayerSeasonStatsRepository playerSeasonStatsRepository;

    @Mock
    private PlayerCareerStatsRepository playerCareerStatsRepository;

    @Mock
    private PlayerAchievementRepository playerAchievementRepository;

    @Mock
    private PlayerTransferHistoryRepository playerTransferHistoryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SeasonService seasonService;

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private PlayerHistoryService playerHistoryService;

    private Player player;
    private Season season;
    private Club club;
    private Team team;

    @BeforeEach
    public void setup() {
        player = new Player();
        player.setId(1L);
        player.setName("John");
        player.setSurname("Doe");

        season = new Season();
        season.setId(1L);
        season.setCurrent(true);

        team = new Team();
        team.setId(1L);
        player.setTeam(team);

        club = new Club();
        club.setId(1L);
        club.setTeam(team);
        club.setLeague(new League());
    }

    @Test
    public void testInitializeSeasonStats() {
        when(playerSeasonStatsRepository.findByPlayerAndSeason(player, season)).thenReturn(Optional.empty());
        when(clubRepository.findByTeam(team)).thenReturn(Optional.of(club));
        when(playerSeasonStatsRepository.save(any(PlayerSeasonStats.class))).thenAnswer(i -> i.getArguments()[0]);

        PlayerSeasonStats stats = playerHistoryService.initializeSeasonStats(player, season);

        assertNotNull(stats);
        assertEquals(player, stats.getPlayer());
        assertEquals(season, stats.getSeason());
        assertEquals(club, stats.getClub());
        assertEquals(0, stats.getMatchesPlayed());
    }

    @Test
    public void testUpdateMatchStatistics() {
        when(seasonService.getCurrentSeason()).thenReturn(season);
        when(playerSeasonStatsRepository.findByPlayerAndSeason(player, season)).thenReturn(Optional.empty());
        when(clubRepository.findByTeam(team)).thenReturn(Optional.of(club));
        when(playerSeasonStatsRepository.save(any(PlayerSeasonStats.class))).thenAnswer(i -> i.getArguments()[0]);
        when(playerSeasonStatsRepository.findByPlayer(player)).thenReturn(java.util.Collections.emptyList());

        com.lollito.fm.model.MatchPlayerStats matchStats = com.lollito.fm.model.MatchPlayerStats.builder()
                .player(player)
                .goals(1)
                .minutesPlayed(90)
                .rating(8.0)
                .build();

        playerHistoryService.updateMatchStatistics(player, matchStats);

        // Verification is tricky with save(any), but we can verify calls
        org.mockito.Mockito.verify(playerSeasonStatsRepository, org.mockito.Mockito.times(2)).save(any(PlayerSeasonStats.class));
        org.mockito.Mockito.verify(playerCareerStatsRepository, org.mockito.Mockito.atLeast(1)).save(any(com.lollito.fm.model.PlayerCareerStats.class));
    }
}
