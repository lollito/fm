package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchPlayerStats;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Watchlist;
import com.lollito.fm.model.WatchlistCategory;
import com.lollito.fm.model.WatchlistEntry;
import com.lollito.fm.model.WatchlistNotification;
import com.lollito.fm.model.WatchlistPriority;
import com.lollito.fm.repository.MatchPlayerStatsRepository;
import com.lollito.fm.repository.WatchlistEntryRepository;
import com.lollito.fm.repository.WatchlistNotificationRepository;
import com.lollito.fm.repository.WatchlistRepository;
import com.lollito.fm.repository.WatchlistUpdateRepository;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private WatchlistEntryRepository watchlistEntryRepository;

    @Mock
    private WatchlistNotificationRepository notificationRepository;

    @Mock
    private WatchlistUpdateRepository updateRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private ClubService clubService;

    @Mock
    private MatchPlayerStatsRepository matchPlayerStatsRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    @Test
    void testAddPlayerToWatchlist() {
        // Setup config values
        ReflectionTestUtils.setField(watchlistService, "defaultMaxEntries", 50);
        ReflectionTestUtils.setField(watchlistService, "valueChangeThreshold", 0.05);

        Club club = createTestClub();
        Player player = createTestPlayer();
        Watchlist watchlist = createTestWatchlist(club);

        when(clubService.findById(club.getId())).thenReturn(club);
        when(watchlistRepository.findByClub(club)).thenReturn(Optional.of(watchlist));
        when(watchlistEntryRepository.findByWatchlistAndPlayer(watchlist, player))
            .thenReturn(Optional.empty());
        when(playerService.findOne(player.getId())).thenReturn(player);
        when(watchlistEntryRepository.save(any(WatchlistEntry.class))).thenAnswer(i -> i.getArguments()[0]);

        AddToWatchlistRequest request = new AddToWatchlistRequest();
        request.setPriority(WatchlistPriority.HIGH);
        request.setCategory(WatchlistCategory.TARGET);
        request.setNotes("Promising young striker");

        WatchlistEntry entry = watchlistService.addPlayerToWatchlist(
            club.getId(), player.getId(), request);

        assertThat(entry.getPlayer()).isEqualTo(player);
        assertThat(entry.getPriority()).isEqualTo(WatchlistPriority.HIGH);
        assertThat(entry.getNotes()).isEqualTo("Promising young striker");
    }

    @Test
    void testValueChangeNotification() {
        // Setup config values
        ReflectionTestUtils.setField(watchlistService, "defaultMaxEntries", 50);
        ReflectionTestUtils.setField(watchlistService, "valueChangeThreshold", 0.05);

        WatchlistEntry entry = createTestWatchlistEntry();
        entry.setCurrentValue(BigDecimal.valueOf(1000000));
        entry.setNotifyOnPriceChange(true);

        BigDecimal newValue = BigDecimal.valueOf(1200000); // 20% increase

        watchlistService.processValueChange(entry, newValue);

        verify(notificationRepository).save(any(WatchlistNotification.class));
        assertThat(entry.getCurrentValue()).isEqualTo(newValue);
    }

    @Test
    void testWatchlistCapacityLimit() {
        // Setup config values
        ReflectionTestUtils.setField(watchlistService, "defaultMaxEntries", 50);

        Club club = createTestClub();
        Watchlist watchlist = createTestWatchlist(club);
        watchlist.setMaxEntries(2);

        // Add 2 entries to reach capacity
        watchlist.getEntries().add(createTestWatchlistEntry());
        watchlist.getEntries().add(createTestWatchlistEntry());

        when(clubService.findById(club.getId())).thenReturn(club);
        when(watchlistRepository.findByClub(club)).thenReturn(Optional.of(watchlist));
        when(playerService.findOne(1L)).thenReturn(createTestPlayer());

        AddToWatchlistRequest request = new AddToWatchlistRequest();

        assertThatThrownBy(() ->
            watchlistService.addPlayerToWatchlist(club.getId(), 1L, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Watchlist is full");
    }

    @Test
    void testProcessRecentPerformances_NoDuplicateNotifications() {
        // Setup config values
        ReflectionTestUtils.setField(watchlistService, "valueChangeThreshold", 0.05);

        // Setup
        WatchlistEntry entry = createTestWatchlistEntry();
        entry.setNotifyOnPerformance(true);
        // Ensure initial values match current values to avoid other notifications
        entry.setAddedValue(BigDecimal.ZERO);
        entry.setCurrentValue(BigDecimal.ZERO);
        entry.setAddedRating(0.0);
        entry.setCurrentRating(0.0);

        // Mock calculatePlayerValue to return 0 to match
        // But calculatePlayerValue uses player.getAverage(), so let's set player stats to 0
        entry.getPlayer().setStamina(0.0); // Assuming average depends on stats.
        // Or simpler: processWatchlistEntryUpdates calls calculatePlayerValue(player).
        // Since we can't easily mock private method, let's rely on null/0 checks.
        // Actually, createTestPlayer sets Stamina 80.
        // calculatePlayerValue returns (avg * 1000000).
        // If we set entry.currentValue to that value, no change will be detected.
        // But calculation is internal.

        // Better approach: mock findAllActive to return our entry.
        when(watchlistEntryRepository.findAllActive()).thenReturn(List.of(entry));

        Match match = new Match();
        match.setId(100L);
        Club home = new Club(); home.setName("Home FC"); match.setHome(home);
        Club away = new Club(); away.setName("Away FC"); match.setAway(away);

        MatchPlayerStats stats = new MatchPlayerStats();
        stats.setMatch(match);
        stats.setGoals(3); // Notable performance (hat-trick)
        stats.setRating(9.5);

        when(matchPlayerStatsRepository.findRecentStats(any(), any())).thenReturn(List.of(stats));

        // Case 1: Notification already exists
        when(notificationRepository.existsByWatchlistEntryAndMatchId(entry, match.getId())).thenReturn(true);

        watchlistService.processDailyWatchlistUpdates();

        verify(notificationRepository, never()).save(any(WatchlistNotification.class));

        // Case 2: Notification does not exist
        when(notificationRepository.existsByWatchlistEntryAndMatchId(entry, match.getId())).thenReturn(false);

        watchlistService.processDailyWatchlistUpdates();

        verify(notificationRepository).save(any(WatchlistNotification.class));
    }

    private Club createTestClub() {
        Club club = new Club();
        club.setId(1L);
        club.setName("Test Club");
        return club;
    }

    private Player createTestPlayer() {
        Player player = new Player();
        player.setId(1L);
        player.setName("John");
        player.setSurname("Doe");
        player.setBirth(LocalDate.of(2000, 1, 1));
        // Set stats for value/rating calculation
        player.setStamina(80.0);
        return player;
    }

    private Watchlist createTestWatchlist(Club club) {
        Watchlist watchlist = new Watchlist();
        watchlist.setId(1L);
        watchlist.setClub(club);
        watchlist.setMaxEntries(50);
        watchlist.setEntries(new ArrayList<>());
        return watchlist;
    }

    private WatchlistEntry createTestWatchlistEntry() {
        WatchlistEntry entry = new WatchlistEntry();
        entry.setId(1L);
        entry.setPlayer(createTestPlayer());
        entry.setWatchlist(createTestWatchlist(createTestClub()));
        entry.setTotalNotifications(0);
        return entry;
    }
}
