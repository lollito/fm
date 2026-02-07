package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutingReport;
import com.lollito.fm.repository.ScoutingReportRepository;

@ExtendWith(MockitoExtension.class)
class ScoutingServiceTest {

    @Mock
    private ScoutingReportRepository reportRepository;

    @Mock
    private WatchlistService watchlistService;

    // Other mocks to satisfy ScoutingService dependencies (though not used in addToWatchlist)
    @Mock private com.lollito.fm.repository.ScoutRepository scoutRepository;
    @Mock private com.lollito.fm.repository.ScoutingAssignmentRepository assignmentRepository;
    @Mock private com.lollito.fm.repository.PlayerScoutingStatusRepository scoutingStatusRepository;
    @Mock private PlayerService playerService;
    @Mock private ClubService clubService;

    @InjectMocks
    private ScoutingService scoutingService;

    @Test
    void testAddToWatchlist() {
        Long reportId = 1L;
        String notes = "Check this player";

        ScoutingReport report = new ScoutingReport();
        report.setId(reportId);

        Player player = new Player();
        player.setId(10L);
        report.setPlayer(player);

        Club club = new Club();
        club.setId(100L);

        Scout scout = new Scout();
        scout.setClub(club);
        report.setScout(scout);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        scoutingService.addToWatchlist(reportId, notes);

        verify(watchlistService).addPlayerToWatchlist(eq(100L), eq(10L), any(AddToWatchlistRequest.class));
    }
}
