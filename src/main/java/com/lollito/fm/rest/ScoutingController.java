package com.lollito.fm.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.dto.AssignPlayerScoutingRequest;
import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.dto.PlayerScoutingStatusDTO;
import com.lollito.fm.dto.ScoutDTO;
import com.lollito.fm.dto.ScoutingAssignmentDTO;
import com.lollito.fm.dto.ScoutingRecommendationDTO;
import com.lollito.fm.dto.ScoutingReportDTO;
import com.lollito.fm.mapper.ScoutingMapper;
import com.lollito.fm.model.AssignmentStatus;
import com.lollito.fm.model.PlayerScoutingStatus;
import com.lollito.fm.model.RecommendationLevel;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutingAssignment;
import com.lollito.fm.model.ScoutingReport;
import com.lollito.fm.service.ScoutingService;

@RestController
@RequestMapping("/api/scouting")
public class ScoutingController {

    @Autowired
    private ScoutingService scoutingService;

    @Autowired
    private ScoutingMapper scoutingMapper;

    @GetMapping("/club/{clubId}/scouts")
    public ResponseEntity<List<ScoutDTO>> getClubScouts(@PathVariable Long clubId) {
        List<Scout> scouts = scoutingService.getClubScouts(clubId);
        return ResponseEntity.ok(scouts.stream()
            .map(scoutingMapper::toDto)
            .collect(Collectors.toList()));
    }

    @PostMapping("/assignment/player")
    public ResponseEntity<ScoutingAssignmentDTO> assignPlayerScouting(
            @RequestBody AssignPlayerScoutingRequest request) {
        ScoutingAssignment assignment = scoutingService.assignPlayerScouting(
            request.getScoutId(),
            request.getPlayerId(),
            request.getPriority(),
            request.getInstructions()
        );
        return ResponseEntity.ok(scoutingMapper.toDto(assignment));
    }

    @GetMapping("/club/{clubId}/assignments")
    public ResponseEntity<List<ScoutingAssignmentDTO>> getClubAssignments(
            @PathVariable Long clubId,
            @RequestParam(required = false) AssignmentStatus status) {
        List<ScoutingAssignment> assignments = scoutingService.getClubAssignments(clubId, status);
        return ResponseEntity.ok(assignments.stream()
            .map(scoutingMapper::toDto)
            .collect(Collectors.toList()));
    }

    @GetMapping("/player/{playerId}/status/{clubId}")
    public ResponseEntity<PlayerScoutingStatusDTO> getPlayerScoutingStatus(
            @PathVariable Long playerId,
            @PathVariable Long clubId) {
        PlayerScoutingStatus status = scoutingService.getPlayerScoutingStatus(playerId, clubId);
        return ResponseEntity.ok(scoutingMapper.toDto(status));
    }

    @GetMapping("/player/{playerId}/revealed/{clubId}")
    public ResponseEntity<PlayerDTO> getRevealedPlayerInfo(
            @PathVariable Long playerId,
            @PathVariable Long clubId) {
        PlayerDTO player = scoutingService.getRevealedPlayerInfo(playerId, clubId);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/club/{clubId}/reports")
    public ResponseEntity<Page<ScoutingReportDTO>> getScoutingReports(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RecommendationLevel recommendation) {
        Page<ScoutingReport> reports = scoutingService.getScoutingReports(
            clubId, PageRequest.of(page, size), recommendation);
        return ResponseEntity.ok(reports.map(scoutingMapper::toDto));
    }

    @GetMapping("/club/{clubId}/recommendations")
    public ResponseEntity<List<ScoutingRecommendationDTO>> getScoutingRecommendations(
            @PathVariable Long clubId) {
        List<ScoutingRecommendationDTO> recommendations = scoutingService
            .getScoutingRecommendations(clubId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/report/{reportId}/watchlist")
    public ResponseEntity<Void> addToWatchlist(
            @PathVariable Long reportId,
            @RequestBody AddToWatchlistRequest request) {
        scoutingService.addToWatchlist(reportId, request.getNotes());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/assignment/{assignmentId}/cancel")
    public ResponseEntity<Void> cancelAssignment(@PathVariable Long assignmentId) {
        scoutingService.cancelAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }
}
