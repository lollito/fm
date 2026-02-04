package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.PlayerAchievement;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.PlayerTransferHistory;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.rest.AddAchievementRequest;
import com.lollito.fm.model.rest.PlayerAchievementDTO;
import com.lollito.fm.model.rest.PlayerHistoryDTO;
import com.lollito.fm.model.rest.PlayerSeasonStatsDTO;
import com.lollito.fm.model.rest.PlayerTransferHistoryDTO;
import com.lollito.fm.service.PlayerHistoryService;
import com.lollito.fm.service.SeasonService;

@RestController
@RequestMapping("/api/player-history")
public class PlayerHistoryController {

    @Autowired
    private PlayerHistoryService playerHistoryService;

    @Autowired
    private SeasonService seasonService;

    @GetMapping("/player/{playerId}")
    public ResponseEntity<PlayerHistoryDTO> getPlayerHistory(@PathVariable Long playerId) {
        PlayerHistoryDTO history = playerHistoryService.getPlayerHistory(playerId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/player/{playerId}/season/{seasonId}")
    public ResponseEntity<PlayerSeasonStatsDTO> getPlayerSeasonStats(
            @PathVariable Long playerId,
            @PathVariable Long seasonId) {
        PlayerSeasonStats stats = playerHistoryService.getPlayerSeasonStats(playerId, seasonId);
        return ResponseEntity.ok(playerHistoryService.convertToDTO(stats));
    }

    @GetMapping("/player/{playerId}/achievements")
    public ResponseEntity<List<PlayerAchievementDTO>> getPlayerAchievements(@PathVariable Long playerId) {
        List<PlayerAchievement> achievements = playerHistoryService.getPlayerAchievements(playerId);
        return ResponseEntity.ok(achievements.stream()
            .map(playerHistoryService::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/player/{playerId}/transfers")
    public ResponseEntity<List<PlayerTransferHistoryDTO>> getPlayerTransfers(@PathVariable Long playerId) {
        List<PlayerTransferHistory> transfers = playerHistoryService.getPlayerTransfers(playerId);
        return ResponseEntity.ok(transfers.stream()
            .map(playerHistoryService::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/league/{leagueId}/top-scorers")
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getTopScorers(
            @PathVariable Long leagueId,
            @RequestParam(required = false) Long seasonId,
            @RequestParam(defaultValue = "20") int limit) {
        Season season = seasonId != null ? seasonService.findById(seasonId) : seasonService.getCurrentSeason();
        List<PlayerSeasonStats> topScorers = playerHistoryService.getLeagueTopScorers(leagueId, season, limit);
        return ResponseEntity.ok(topScorers.stream()
            .map(playerHistoryService::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/league/{leagueId}/top-assists")
    public ResponseEntity<List<PlayerSeasonStatsDTO>> getTopAssists(
            @PathVariable Long leagueId,
            @RequestParam(required = false) Long seasonId,
            @RequestParam(defaultValue = "20") int limit) {
        Season season = seasonId != null ? seasonService.findById(seasonId) : seasonService.getCurrentSeason();
        List<PlayerSeasonStats> topAssists = playerHistoryService.getLeagueTopAssists(leagueId, season, limit);
        return ResponseEntity.ok(topAssists.stream()
            .map(playerHistoryService::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/player/{playerId}/achievement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerAchievementDTO> addAchievement(
            @PathVariable Long playerId,
            @RequestBody AddAchievementRequest request) {
        PlayerAchievement achievement = playerHistoryService.addAchievement(
            playerId, request.getType(), request.getTitle(), request.getDescription());
        return ResponseEntity.ok(playerHistoryService.convertToDTO(achievement));
    }
}
