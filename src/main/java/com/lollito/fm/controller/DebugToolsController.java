package com.lollito.fm.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.DebugAction;
import com.lollito.fm.model.DebugActionStatus;
import com.lollito.fm.model.DebugActionType;
import com.lollito.fm.model.MetricType;
import com.lollito.fm.model.SystemSnapshot;
import com.lollito.fm.model.TestExecution;
import com.lollito.fm.model.TestScenario;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.AdjustFinancesRequest;
import com.lollito.fm.model.dto.AdvanceSeasonRequest;
import com.lollito.fm.model.dto.CreateSnapshotRequest;
import com.lollito.fm.model.dto.DebugActionDTO;
import com.lollito.fm.model.dto.DebugActionResult;
import com.lollito.fm.model.dto.DebugDashboardDTO;
import com.lollito.fm.model.dto.MetricFilter;
import com.lollito.fm.model.dto.ModifyPlayerStatsRequest;
import com.lollito.fm.model.dto.PerformanceMetricDTO;
import com.lollito.fm.model.dto.SimulateMatchesRequest;
import com.lollito.fm.model.dto.SystemSnapshotDTO;
import com.lollito.fm.model.dto.TestExecutionDTO;
import com.lollito.fm.model.dto.TestScenarioDTO;
import com.lollito.fm.service.DebugToolsService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping("/api/admin/debug")
public class DebugToolsController {

    @Autowired
    private DebugToolsService debugToolsService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<DebugDashboardDTO> getDebugDashboard() {
        DebugDashboardDTO dashboard = debugToolsService.getDebugDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/season/advance")
    public ResponseEntity<DebugActionResult> advanceSeason(
            @RequestBody AdvanceSeasonRequest request,
            Authentication authentication) {
        User adminUser = userService.getUser(authentication.getName());
        DebugActionResult result = debugToolsService.advanceSeason(request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/matches/simulate")
    public ResponseEntity<DebugActionResult> simulateMatches(
            @RequestBody SimulateMatchesRequest request,
            Authentication authentication) {
        User adminUser = userService.getUser(authentication.getName());
        DebugActionResult result = debugToolsService.simulateMatches(request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/players/modify-stats")
    public ResponseEntity<DebugActionResult> modifyPlayerStats(
            @RequestBody ModifyPlayerStatsRequest request,
            Authentication authentication) {
        User adminUser = userService.getUser(authentication.getName());
        DebugActionResult result = debugToolsService.modifyPlayerStats(request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/finances/adjust")
    public ResponseEntity<DebugActionResult> adjustFinances(
            @RequestBody AdjustFinancesRequest request,
            Authentication authentication) {
        User adminUser = userService.getUser(authentication.getName());
        DebugActionResult result = debugToolsService.adjustFinances(request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/snapshots")
    public ResponseEntity<SystemSnapshotDTO> createSnapshot(
            @RequestBody CreateSnapshotRequest request,
            Authentication authentication) {
        User adminUser = userService.getUser(authentication.getName());
        SystemSnapshot snapshot = debugToolsService.createSystemSnapshot(request, adminUser);
        return ResponseEntity.ok(convertToDTO(snapshot));
    }

    @GetMapping("/snapshots")
    public ResponseEntity<List<SystemSnapshotDTO>> getSnapshots() {
        List<SystemSnapshot> snapshots = debugToolsService.getSystemSnapshots();
        return ResponseEntity.ok(snapshots.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/test-scenarios/{scenarioId}/execute")
    public ResponseEntity<TestExecutionDTO> executeTestScenario(
            @PathVariable Long scenarioId,
            Authentication authentication) {
        User adminUser = userService.getUser(authentication.getName());
        TestExecution execution = debugToolsService.executeTestScenario(scenarioId, adminUser);
        return ResponseEntity.ok(convertToDTO(execution));
    }

    @GetMapping("/test-scenarios")
    public ResponseEntity<List<TestScenarioDTO>> getTestScenarios() {
        List<TestScenario> scenarios = debugToolsService.getTestScenarios();
        return ResponseEntity.ok(scenarios.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<PerformanceMetricDTO>> getPerformanceMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) List<MetricType> metricTypes) {

        MetricFilter filter = MetricFilter.builder()
            .startTime(startTime)
            .endTime(endTime)
            .metricTypes(metricTypes != null ? metricTypes : Arrays.asList(MetricType.values()))
            .build();

        List<PerformanceMetricDTO> metrics = debugToolsService.getPerformanceMetrics(filter);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/actions")
    public ResponseEntity<Page<DebugActionDTO>> getDebugActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) DebugActionType actionType,
            @RequestParam(required = false) DebugActionStatus status) {

        Page<DebugAction> actions = debugToolsService.getDebugActions(
            PageRequest.of(page, size, Sort.by("executedAt").descending()),
            actionType, status);

        return ResponseEntity.ok(actions.map(this::convertToDTO));
    }

    // Converters

    private DebugActionDTO convertToDTO(DebugAction action) {
        return DebugActionDTO.builder()
            .id(action.getId())
            .adminUsername(action.getAdminUser() != null ? action.getAdminUser().getUsername() : "Unknown")
            .actionType(action.getActionType())
            .actionName(action.getActionName())
            .actionDescription(action.getActionDescription())
            .targetEntityType(action.getTargetEntityType())
            .targetEntityId(action.getTargetEntityId())
            .targetEntityName(action.getTargetEntityName())
            .executedAt(action.getExecutedAt())
            .status(action.getStatus())
            .result(action.getResult())
            .impactSummary(action.getImpactSummary())
            .build();
    }

    private SystemSnapshotDTO convertToDTO(SystemSnapshot snapshot) {
        return SystemSnapshotDTO.builder()
            .id(snapshot.getId())
            .snapshotName(snapshot.getSnapshotName())
            .description(snapshot.getDescription())
            .snapshotType(snapshot.getSnapshotType())
            .createdAt(snapshot.getCreatedAt())
            .createdBy(snapshot.getCreatedBy())
            .scopes(snapshot.getScopes())
            .status(snapshot.getStatus())
            .isRestorable(snapshot.getIsRestorable())
            .build();
    }

    private TestScenarioDTO convertToDTO(TestScenario scenario) {
         return TestScenarioDTO.builder()
            .id(scenario.getId())
            .scenarioName(scenario.getScenarioName())
            .description(scenario.getDescription())
            .category(scenario.getCategory())
            .scenarioConfig(scenario.getScenarioConfig())
            .expectedOutcomes(scenario.getExpectedOutcomes())
            .isActive(scenario.getIsActive())
            .createdBy(scenario.getCreatedBy())
            .createdAt(scenario.getCreatedAt())
            .build();
    }

    private TestExecutionDTO convertToDTO(TestExecution execution) {
        return TestExecutionDTO.builder()
            .id(execution.getId())
            .scenarioId(execution.getTestScenario().getId())
            .scenarioName(execution.getTestScenario().getScenarioName())
            .executedBy(execution.getExecutedBy().getUsername())
            .startedAt(execution.getStartedAt())
            .completedAt(execution.getCompletedAt())
            .executionTimeMs(execution.getExecutionTimeMs())
            .status(execution.getStatus())
            .passed(execution.getPassed())
            .failureReason(execution.getFailureReason())
            .executionResults(execution.getExecutionResults())
            .actualOutcomes(execution.getActualOutcomes())
            .build();
    }
}
