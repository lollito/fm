package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.mapper.TrainingMapper;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTrainingResult;
import com.lollito.fm.model.TrainingPlan;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.IndividualFocusDTO;
import com.lollito.fm.model.dto.IndividualFocusRequest;
import com.lollito.fm.model.dto.ManualTrainingRequest;
import com.lollito.fm.model.dto.PlayerTrainingResultDTO;
import com.lollito.fm.model.dto.TrainingPlanDTO;
import com.lollito.fm.model.dto.TrainingPlanRequest;
import com.lollito.fm.model.dto.TrainingSessionDTO;
import com.lollito.fm.model.PlayerTrainingFocus;
import com.lollito.fm.service.TrainingService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.lollito.fm.service.PlayerService playerService;

    @Autowired
    private TrainingMapper trainingMapper;

    @GetMapping("/plan/{teamId}")
    public ResponseEntity<TrainingPlanDTO> getTrainingPlan(
            @PathVariable Long teamId) {
        TrainingPlan plan = trainingService.getTrainingPlan(teamId);
        return ResponseEntity.ok(trainingMapper.toDto(plan));
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
        return ResponseEntity.ok(trainingMapper.toDto(plan));
    }

    @GetMapping("/history/{teamId}")
    public ResponseEntity<Page<TrainingSessionDTO>> getTrainingHistory(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TrainingSession> sessions = trainingService
            .getTrainingHistory(teamId, PageRequest.of(page, size));
        return ResponseEntity.ok(sessions.map(trainingMapper::toDto));
    }

    @GetMapping("/session/{sessionId}/results")
    public ResponseEntity<List<PlayerTrainingResultDTO>> getSessionResults(
            @PathVariable Long sessionId) {
        List<PlayerTrainingResult> results = trainingService
            .getSessionResults(sessionId);
        return ResponseEntity.ok(results.stream()
            .map(trainingMapper::toDto)
            .collect(Collectors.toList()));
    }

    @PostMapping("/session/manual/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainingSessionDTO> createManualSession(
            @PathVariable Long teamId,
            @RequestBody ManualTrainingRequest request) {
        TrainingSession session = trainingService
            .createManualTrainingSession(teamId, request);
        return ResponseEntity.ok(trainingMapper.toDto(session));
    }

    @PostMapping("/focus/{playerId}")
    public ResponseEntity<IndividualFocusDTO> assignIndividualFocus(
            @PathVariable Long playerId,
            @RequestBody IndividualFocusRequest request) {
        User user = userService.getLoggedUser();
        Player player = playerService.findOne(playerId);

        if (user.getClub() == null || !user.getClub().getTeam().getId().equals(player.getTeam().getId())) {
            return ResponseEntity.status(403).build();
        }

        try {
            PlayerTrainingFocus focus = trainingService.assignIndividualFocus(playerId, request);
            IndividualFocusDTO dto = IndividualFocusDTO.builder()
                .id(focus.getId())
                .playerId(focus.getPlayer().getId())
                .playerName(focus.getPlayer().getName() + " " + focus.getPlayer().getSurname())
                .focus(focus.getFocus())
                .intensity(focus.getIntensity())
                .startDate(focus.getStartDate())
                .endDate(focus.getEndDate())
                .build();
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/focus/{playerId}")
    public ResponseEntity<Void> removeIndividualFocus(@PathVariable Long playerId) {
        User user = userService.getLoggedUser();
        Player player = playerService.findOne(playerId);

        if (user.getClub() == null || !user.getClub().getTeam().getId().equals(player.getTeam().getId())) {
            return ResponseEntity.status(403).build();
        }

        trainingService.removeIndividualFocus(playerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/focus")
    public ResponseEntity<List<IndividualFocusDTO>> getTeamIndividualFocuses() {
        User user = userService.getLoggedUser();
        if (user.getClub() == null) {
            return ResponseEntity.badRequest().build();
        }
        Long teamId = user.getClub().getTeam().getId();
        return ResponseEntity.ok(trainingService.getTeamIndividualFocuses(teamId));
    }
}
