package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.PlayerTrainingResult;
import com.lollito.fm.model.TrainingPlan;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.dto.ManualTrainingRequest;
import com.lollito.fm.model.dto.PlayerTrainingResultDTO;
import com.lollito.fm.model.dto.TrainingPlanDTO;
import com.lollito.fm.model.dto.TrainingPlanRequest;
import com.lollito.fm.model.dto.TrainingSessionDTO;
import com.lollito.fm.service.TrainingService;
import com.lollito.fm.service.UserService;
import com.lollito.fm.model.User;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private UserService userService;

    @GetMapping("/plan/{teamId}")
    public ResponseEntity<TrainingPlanDTO> getTrainingPlan(
            @PathVariable Long teamId) {
        TrainingPlan plan = trainingService.getTrainingPlan(teamId);
        return ResponseEntity.ok(convertToDTO(plan));
    }

    @PutMapping("/plan/{teamId}")
    public ResponseEntity<TrainingPlanDTO> updateTrainingPlan(
            @PathVariable Long teamId,
            @RequestBody TrainingPlanRequest request) {
        User user = userService.getLoggedUser();
        if (user.getClub() == null || !user.getClub().getTeam().getId().equals(teamId)) {
            return ResponseEntity.status(403).build();
        }
        TrainingPlan plan = trainingService.updateTrainingPlan(teamId, request);
        return ResponseEntity.ok(convertToDTO(plan));
    }

    @GetMapping("/history/{teamId}")
    public ResponseEntity<Page<TrainingSessionDTO>> getTrainingHistory(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TrainingSession> sessions = trainingService
            .getTrainingHistory(teamId, PageRequest.of(page, size));
        return ResponseEntity.ok(sessions.map(this::convertToDTO));
    }

    @GetMapping("/session/{sessionId}/results")
    public ResponseEntity<List<PlayerTrainingResultDTO>> getSessionResults(
            @PathVariable Long sessionId) {
        List<PlayerTrainingResult> results = trainingService
            .getSessionResults(sessionId);
        return ResponseEntity.ok(results.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/session/manual/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingSessionDTO> createManualSession(
            @PathVariable Long teamId,
            @RequestBody ManualTrainingRequest request) {
        TrainingSession session = trainingService
            .createManualTrainingSession(teamId, request);
        return ResponseEntity.ok(convertToDTO(session));
    }

    private TrainingPlanDTO convertToDTO(TrainingPlan plan) {
        TrainingPlanDTO dto = new TrainingPlanDTO();
        dto.setId(plan.getId());
        dto.setTeamId(plan.getTeam().getId());
        dto.setMondayFocus(plan.getMondayFocus());
        dto.setTuesdayFocus(plan.getTuesdayFocus());
        dto.setWednesdayFocus(plan.getWednesdayFocus());
        dto.setThursdayFocus(plan.getThursdayFocus());
        dto.setFridayFocus(plan.getFridayFocus());
        dto.setIntensity(plan.getIntensity());
        dto.setRestOnWeekends(plan.getRestOnWeekends());
        dto.setLastUpdated(plan.getLastUpdated());
        return dto;
    }

    private TrainingSessionDTO convertToDTO(TrainingSession session) {
        TrainingSessionDTO dto = new TrainingSessionDTO();
        dto.setId(session.getId());
        dto.setTeamId(session.getTeam().getId());
        dto.setFocus(session.getFocus());
        dto.setIntensity(session.getIntensity());
        dto.setStartDate(session.getStartDate());
        dto.setEndDate(session.getEndDate());
        dto.setStatus(session.getStatus());
        dto.setEffectivenessMultiplier(session.getEffectivenessMultiplier());
        if (session.getPlayerResults() != null) {
            // We want to avoid full details in history list if possible, but here we don't have control over context easily.
            // For now, mapping results is fine, but beware of LazyInitializationException if session was loaded without results.
            // But getTrainingHistory usually loads lazily.
            // session.getPlayerResults() is a collection. Accessing it triggers loading.
            // If the transaction is closed, it will fail.
            // Controllers are outside transaction usually.
            // But `enable_lazy_load_no_trans=true` is set in memory!
            // So it should work.
             dto.setPlayerResults(session.getPlayerResults().stream()
                 .map(this::convertToDTO)
                 .collect(Collectors.toList()));
        }
        return dto;
    }

    private PlayerTrainingResultDTO convertToDTO(PlayerTrainingResult result) {
        PlayerTrainingResultDTO dto = new PlayerTrainingResultDTO();
        dto.setId(result.getId());
        dto.setTrainingSessionId(result.getTrainingSession().getId());

        PlayerTrainingResultDTO.PlayerSummaryDTO playerDTO = new PlayerTrainingResultDTO.PlayerSummaryDTO();
        playerDTO.setId(result.getPlayer().getId());
        playerDTO.setName(result.getPlayer().getName());
        playerDTO.setSurname(result.getPlayer().getSurname());
        dto.setPlayer(playerDTO);

        dto.setAttendanceRate(result.getAttendanceRate());
        dto.setImprovementGained(result.getImprovementGained());
        dto.setFatigueGained(result.getFatigueGained());
        dto.setPerformance(result.getPerformance());
        return dto;
    }
}
