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

    @GetMapping("/club/{clubId}/scouts")
    public ResponseEntity<List<ScoutDTO>> getClubScouts(@PathVariable Long clubId) {
        List<Scout> scouts = scoutingService.getClubScouts(clubId);
        return ResponseEntity.ok(scouts.stream()
            .map(this::convertToDTO)
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
        return ResponseEntity.ok(convertToDTO(assignment));
    }

    @GetMapping("/club/{clubId}/assignments")
    public ResponseEntity<List<ScoutingAssignmentDTO>> getClubAssignments(
            @PathVariable Long clubId,
            @RequestParam(required = false) AssignmentStatus status) {
        List<ScoutingAssignment> assignments = scoutingService.getClubAssignments(clubId, status);
        return ResponseEntity.ok(assignments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/player/{playerId}/status/{clubId}")
    public ResponseEntity<PlayerScoutingStatusDTO> getPlayerScoutingStatus(
            @PathVariable Long playerId,
            @PathVariable Long clubId) {
        PlayerScoutingStatus status = scoutingService.getPlayerScoutingStatus(playerId, clubId);
        return ResponseEntity.ok(convertToDTO(status));
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
        return ResponseEntity.ok(reports.map(this::convertToDTO));
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

    // Mappers

    private ScoutDTO convertToDTO(Scout scout) {
        ScoutDTO dto = new ScoutDTO();
        dto.setId(scout.getId());
        dto.setName(scout.getName());
        dto.setSurname(scout.getSurname());
        if(scout.getClub() != null) dto.setClubId(scout.getClub().getId());
        if(scout.getScoutingRegion() != null) {
            dto.setRegionId(scout.getScoutingRegion().getId());
            dto.setRegionName(scout.getScoutingRegion().getName());
        }
        dto.setAbility(scout.getAbility());
        dto.setReputation(scout.getReputation());
        dto.setMonthlySalary(scout.getMonthlySalary());
        dto.setSpecialization(scout.getSpecialization());
        dto.setStatus(scout.getStatus());
        dto.setContractEnd(scout.getContractEnd());
        dto.setExperience(scout.getExperience());
        return dto;
    }

    private ScoutingAssignmentDTO convertToDTO(ScoutingAssignment assignment) {
        ScoutingAssignmentDTO dto = new ScoutingAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setScout(convertToDTO(assignment.getScout()));
        if(assignment.getTargetPlayer() != null) {
            dto.setTargetPlayerId(assignment.getTargetPlayer().getId());
            dto.setTargetPlayerName(assignment.getTargetPlayer().getName());
            dto.setTargetPlayerSurname(assignment.getTargetPlayer().getSurname());
        }
        dto.setType(assignment.getType());
        dto.setStatus(assignment.getStatus());
        dto.setAssignedDate(assignment.getAssignedDate());
        dto.setCompletionDate(assignment.getCompletionDate());
        dto.setExpectedCompletionDate(assignment.getExpectedCompletionDate());
        dto.setPriority(assignment.getPriority());
        dto.setInstructions(assignment.getInstructions());
        return dto;
    }

    private ScoutingReportDTO convertToDTO(ScoutingReport report) {
        ScoutingReportDTO dto = new ScoutingReportDTO();
        dto.setId(report.getId());
        if(report.getAssignment() != null) dto.setAssignmentId(report.getAssignment().getId());
        if(report.getPlayer() != null) {
            dto.setPlayerId(report.getPlayer().getId());
            dto.setPlayerName(report.getPlayer().getName());
            dto.setPlayerSurname(report.getPlayer().getSurname());
        }
        if(report.getScout() != null) dto.setScout(convertToDTO(report.getScout()));
        dto.setReportDate(report.getReportDate());
        dto.setRevealedStamina(report.getRevealedStamina());
        dto.setRevealedPlaymaking(report.getRevealedPlaymaking());
        dto.setRevealedScoring(report.getRevealedScoring());
        dto.setRevealedWinger(report.getRevealedWinger());
        dto.setRevealedGoalkeeping(report.getRevealedGoalkeeping());
        dto.setRevealedPassing(report.getRevealedPassing());
        dto.setRevealedDefending(report.getRevealedDefending());
        dto.setRevealedSetPieces(report.getRevealedSetPieces());
        dto.setOverallRating(report.getOverallRating());
        dto.setPotentialRating(report.getPotentialRating());
        dto.setAccuracyLevel(report.getAccuracyLevel());
        dto.setRecommendation(report.getRecommendation());
        dto.setStrengths(report.getStrengths());
        dto.setWeaknesses(report.getWeaknesses());
        dto.setPersonalityAssessment(report.getPersonalityAssessment());
        dto.setInjuryHistory(report.getInjuryHistory());
        dto.setEstimatedValue(report.getEstimatedValue());
        dto.setEstimatedWage(report.getEstimatedWage());
        dto.setIsAvailableForTransfer(report.getIsAvailableForTransfer());
        dto.setContractExpiry(report.getContractExpiry());
        dto.setAdditionalNotes(report.getAdditionalNotes());
        dto.setConfidenceLevel(report.getConfidenceLevel());
        return dto;
    }

    private PlayerScoutingStatusDTO convertToDTO(PlayerScoutingStatus status) {
        PlayerScoutingStatusDTO dto = new PlayerScoutingStatusDTO();
        dto.setId(status.getId());
        if(status.getPlayer() != null) dto.setPlayerId(status.getPlayer().getId());
        if(status.getScoutingClub() != null) dto.setClubId(status.getScoutingClub().getId());
        dto.setScoutingLevel(status.getScoutingLevel());
        dto.setLastScoutedDate(status.getLastScoutedDate());
        dto.setFirstScoutedDate(status.getFirstScoutedDate());
        dto.setTimesScoutedThisSeason(status.getTimesScoutedThisSeason());
        dto.setKnowledgeAccuracy(status.getKnowledgeAccuracy());
        dto.setKnownStamina(status.getKnownStamina());
        dto.setKnownPlaymaking(status.getKnownPlaymaking());
        dto.setKnownScoring(status.getKnownScoring());
        dto.setKnownWinger(status.getKnownWinger());
        dto.setKnownGoalkeeping(status.getKnownGoalkeeping());
        dto.setKnownPassing(status.getKnownPassing());
        dto.setKnownDefending(status.getKnownDefending());
        dto.setKnownSetPieces(status.getKnownSetPieces());
        return dto;
    }
}
