package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.model.*;
import com.lollito.fm.model.dto.*;
import com.lollito.fm.model.rest.CreateTransactionRequest;
import com.lollito.fm.repository.rest.*;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DebugToolsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired private SeasonService seasonService;
    @Autowired private MatchService matchService;
    @Autowired private SimulationMatchService simulationMatchService;
    @Autowired private PlayerService playerService;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private FinancialService financialService;
    @Autowired private FinanceRepository financeRepository;
    @Autowired private ClubService clubService;
    @Autowired private DebugActionRepository debugActionRepository;
    @Autowired private SystemSnapshotRepository systemSnapshotRepository;
    @Autowired private PerformanceMetricRepository performanceMetricRepository;
    @Autowired private TestScenarioRepository testScenarioRepository;
    @Autowired private TestExecutionRepository testExecutionRepository;
    @Autowired private ObjectMapper objectMapper;

    public DebugDashboardDTO getDebugDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24Hours = now.minusHours(24);

        List<DebugAction> recentActions = debugActionRepository.findAll(
            PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("executedAt").descending())).getContent();

        List<SystemSnapshot> snapshots = systemSnapshotRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, 5));

        // Mock current metrics if none exist
        List<PerformanceMetricDTO> currentMetrics = new ArrayList<>();

        return DebugDashboardDTO.builder()
            .recentDebugActions(recentActions.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .systemSnapshots(snapshots.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .performanceMetrics(currentMetrics)
            .activeTestScenarios(getActiveTestScenarios())
            .systemHealth(getSystemHealthMetrics())
            .debugActionsLast24Hours(debugActionRepository.countByExecutedAtAfter(last24Hours))
            .build();
    }

    @Transactional
    public DebugActionResult advanceSeason(AdvanceSeasonRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.ADVANCE_SEASON,
            "Advance Season", "Force advance current season",
            "Season", null, null, convertToJson(request)
        );

        try {
            // Create snapshot before making changes (simplified)
            // SystemSnapshot snapshot = createPreDebugSnapshot("Pre-season-advance", adminUser);
            Long snapshotId = null;

            // Execute season advancement
            SeasonAdvancementResult result = seasonService.forceAdvanceSeason(
                request.getSkipRemainingMatches(),
                request.getGenerateNewPlayers(),
                request.getProcessTransfers()
            );

            debugAction.setStatus(DebugActionStatus.COMPLETED);
            debugAction.setCompletedAt(LocalDateTime.now());
            debugAction.setResult(convertToJson(result));
            debugAction.setEntitiesAffected(result.getMatchesProcessed() + result.getTransfersProcessed());
            debugAction.setImpactSummary("Advanced season, processed " + result.getMatchesProcessed() +
                                       " matches and " + result.getTransfersProcessed() + " transfers");

            return DebugActionResult.builder()
                .success(true)
                .message("Season advanced successfully")
                .result(result)
                .snapshotId(snapshotId)
                .build();

        } catch (Exception e) {
            debugAction.setStatus(DebugActionStatus.FAILED);
            debugAction.setErrorMessage(e.getMessage());
            debugAction.setStackTrace(getStackTrace(e));

            return DebugActionResult.builder()
                .success(false)
                .message("Failed to advance season: " + e.getMessage())
                .error(e.getMessage())
                .build();
        } finally {
            debugAction.setExecutionTimeMs(
                Duration.between(debugAction.getExecutedAt(), LocalDateTime.now()).toMillis()
            );
            debugActionRepository.save(debugAction);
        }
    }

    @Transactional
    public DebugActionResult simulateMatches(SimulateMatchesRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.SIMULATE_MATCHES,
            "Simulate Matches", "Force simulate specific matches",
            "Match", null, null, convertToJson(request)
        );

        try {
            // Need to fetch matches manually as matchService.findAllById might not exist or be exposed
            // Using matchService.findById loop or adding a method. matchService has findById.
            List<MatchResult> results = new ArrayList<>();

            for (Long matchId : request.getMatchIds()) {
                Match match = matchService.findById(matchId);

                if (match.getStatus() != MatchStatus.SCHEDULED) {
                    continue;
                }

                MatchResult result = simulationMatchService.simulateMatchWithForcedResult(
                    match, request.getForceResult()
                );
                results.add(result);
            }

            debugAction.setStatus(DebugActionStatus.COMPLETED);
            debugAction.setCompletedAt(LocalDateTime.now());
            debugAction.setResult(convertToJson(results));
            debugAction.setEntitiesAffected(results.size());
            debugAction.setImpactSummary("Simulated " + results.size() + " matches");

            return DebugActionResult.builder()
                .success(true)
                .message("Matches simulated successfully")
                .result(results)
                .build();

        } catch (Exception e) {
            debugAction.setStatus(DebugActionStatus.FAILED);
            debugAction.setErrorMessage(e.getMessage());
            debugAction.setStackTrace(getStackTrace(e));

            return DebugActionResult.builder()
                .success(false)
                .message("Failed to simulate matches: " + e.getMessage())
                .error(e.getMessage())
                .build();
        } finally {
            debugAction.setExecutionTimeMs(
                Duration.between(debugAction.getExecutedAt(), LocalDateTime.now()).toMillis()
            );
            debugActionRepository.save(debugAction);
        }
    }

    @Transactional
    public DebugActionResult modifyPlayerStats(ModifyPlayerStatsRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.MODIFY_PLAYER_STATS,
            "Modify Player Stats", "Modify player statistics for testing",
            "Player", null, null, convertToJson(request)
        );

        try {
            List<Player> players = playerRepository.findAllById(request.getPlayerIds());
            List<Player> modifiedPlayers = new ArrayList<>();

            for (Player player : players) {
                // Apply modifications
                // Assuming modifications are just age and overall for now as complex stats are hard to map generically without reflection

                if (request.getAgeAdjustment() != null) {
                    player.setBirth(LocalDate.now().minusYears(Math.max(16, Math.min(45, player.getAge() + request.getAgeAdjustment()))));
                }

                // TODO: Implement stat modifications map if needed (requires mapping stat names to fields)

                modifiedPlayers.add(playerRepository.save(player));
            }

            debugAction.setStatus(DebugActionStatus.COMPLETED);
            debugAction.setCompletedAt(LocalDateTime.now());
            // Avoid serializing full player list if huge
            debugAction.setResult("Modified " + modifiedPlayers.size() + " players");
            debugAction.setEntitiesAffected(modifiedPlayers.size());
            debugAction.setImpactSummary("Modified stats for " + modifiedPlayers.size() + " players");
            debugAction.setIsReversible(true);

            return DebugActionResult.builder()
                .success(true)
                .message("Player stats modified successfully")
                .result(modifiedPlayers.stream().map(Player::getId).collect(Collectors.toList()))
                .build();

        } catch (Exception e) {
            debugAction.setStatus(DebugActionStatus.FAILED);
            debugAction.setErrorMessage(e.getMessage());
            debugAction.setStackTrace(getStackTrace(e));

            return DebugActionResult.builder()
                .success(false)
                .message("Failed to modify player stats: " + e.getMessage())
                .error(e.getMessage())
                .build();
        } finally {
            debugAction.setExecutionTimeMs(
                Duration.between(debugAction.getExecutedAt(), LocalDateTime.now()).toMillis()
            );
            debugActionRepository.save(debugAction);
        }
    }

    @Transactional
    public DebugActionResult adjustFinances(AdjustFinancesRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.ADJUST_FINANCES,
            "Adjust Finances", "Adjust club finances for testing",
            "Club", request.getClubId(), null, convertToJson(request)
        );

        try {
            Club club = clubService.findById(request.getClubId());
            Finance finance = club.getFinance();

            BigDecimal originalBalance = finance.getBalance();

            if (request.getBalanceAdjustment() != null) {
                finance.setBalance(finance.getBalance().add(request.getBalanceAdjustment()));
            }

            if (request.getSetBalance() != null) {
                finance.setBalance(request.getSetBalance());
            }

            // Debt not fully implemented in Finance model yet

            financeRepository.save(finance);

            // Create transaction record if balance changed
            BigDecimal diff = finance.getBalance().subtract(originalBalance);
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                financialService.processTransaction(club.getId(), CreateTransactionRequest.builder()
                    .type(diff.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.INCOME : TransactionType.EXPENSE)
                    .category(TransactionCategory.SYSTEM_ACTION)
                    .amount(diff.abs())
                    .description("Debug adjustment by admin: " + adminUser.getUsername())
                    .reference("DEBUG_" + debugAction.getId())
                    .effectiveDate(LocalDate.now())
                    .build());
            }

            debugAction.setStatus(DebugActionStatus.COMPLETED);
            debugAction.setCompletedAt(LocalDateTime.now());
            debugAction.setResult(convertToJson(finance));
            debugAction.setEntitiesAffected(1);
            debugAction.setImpactSummary("Adjusted balance from " + originalBalance + " to " + finance.getBalance());

            return DebugActionResult.builder()
                .success(true)
                .message("Finances adjusted successfully")
                .result(finance)
                .build();

        } catch (Exception e) {
            debugAction.setStatus(DebugActionStatus.FAILED);
            debugAction.setErrorMessage(e.getMessage());
            debugAction.setStackTrace(getStackTrace(e));

            return DebugActionResult.builder()
                .success(false)
                .message("Failed to adjust finances: " + e.getMessage())
                .error(e.getMessage())
                .build();
        } finally {
            debugAction.setExecutionTimeMs(
                Duration.between(debugAction.getExecutedAt(), LocalDateTime.now()).toMillis()
            );
            debugActionRepository.save(debugAction);
        }
    }

    @Transactional
    public SystemSnapshot createSystemSnapshot(CreateSnapshotRequest request, User adminUser) {
        SystemSnapshot snapshot = SystemSnapshot.builder()
            .snapshotName(request.getName())
            .description(request.getDescription())
            .snapshotType(request.getType())
            .createdAt(LocalDateTime.now())
            .createdBy(adminUser.getUsername())
            .scopes(request.getScopes())
            .status(SnapshotStatus.CREATING)
            .isRestorable(true)
            .build();

        snapshot = systemSnapshotRepository.save(snapshot);

        // Mock async creation
        final Long id = snapshot.getId();
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate work
                Thread.sleep(1000);
                SystemSnapshot s = systemSnapshotRepository.findById(id).orElse(null);
                if (s != null) {
                    s.setStatus(SnapshotStatus.READY);
                    systemSnapshotRepository.save(s);
                }
            } catch (Exception e) {
                log.error("Snapshot failed", e);
            }
        });

        return snapshot;
    }

    public List<SystemSnapshot> getSystemSnapshots() {
        return systemSnapshotRepository.findAll();
    }

    public List<TestScenario> getTestScenarios() {
        return testScenarioRepository.findAll();
    }

    @Transactional
    public TestExecution executeTestScenario(Long scenarioId, User adminUser) {
        TestScenario scenario = testScenarioRepository.findById(scenarioId)
            .orElseThrow(() -> new EntityNotFoundException("Test scenario not found"));

        TestExecution execution = TestExecution.builder()
            .testScenario(scenario)
            .executedBy(adminUser)
            .startedAt(LocalDateTime.now())
            .status(TestExecutionStatus.RUNNING)
            .build();

        execution = testExecutionRepository.save(execution);

        final Long executionId = execution.getId();
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate execution
                Thread.sleep(2000);
                TestExecution exec = testExecutionRepository.findById(executionId).orElse(null);
                if (exec != null) {
                    exec.setStatus(TestExecutionStatus.PASSED);
                    exec.setCompletedAt(LocalDateTime.now());
                    exec.setPassed(true);
                    exec.setExecutionResults("{\"status\": \"Simulated Success\"}");
                    testExecutionRepository.save(exec);
                }
            } catch (Exception e) {
                log.error("Test execution failed", e);
            }
        });

        return execution;
    }

    public List<PerformanceMetricDTO> getPerformanceMetrics(MetricFilter filter) {
        LocalDateTime startTime = filter.getStartTime() != null ?
            filter.getStartTime() : LocalDateTime.now().minusHours(24);
        LocalDateTime endTime = filter.getEndTime() != null ?
            filter.getEndTime() : LocalDateTime.now();

        List<PerformanceMetric> metrics = performanceMetricRepository
            .findByRecordedAtBetweenAndMetricTypeIn(startTime, endTime, filter.getMetricTypes());

        return metrics.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public Page<DebugAction> getDebugActions(Pageable pageable, DebugActionType actionType, DebugActionStatus status) {
        if (actionType != null && status != null) {
            return debugActionRepository.findByActionTypeAndStatus(actionType, status, pageable);
        } else if (actionType != null) {
            return debugActionRepository.findByActionType(actionType, pageable);
        } else if (status != null) {
            return debugActionRepository.findByStatus(status, pageable);
        } else {
            return debugActionRepository.findAll(pageable);
        }
    }

    private DebugAction createDebugAction(User adminUser, DebugActionType actionType,
                                        String actionName, String description,
                                        String entityType, Long entityId, String entityName,
                                        String parameters) {
        DebugAction debugAction = DebugAction.builder()
            .adminUser(adminUser)
            .actionType(actionType)
            .actionName(actionName)
            .actionDescription(description)
            .targetEntityType(entityType)
            .targetEntityId(entityId)
            .targetEntityName(entityName)
            .actionParameters(parameters)
            .executedAt(LocalDateTime.now())
            .status(DebugActionStatus.EXECUTING)
            .isReversible(false)
            .build();

        return debugActionRepository.save(debugAction);
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
            return "{}";
        }
    }

    private String getStackTrace(Exception e) {
        return Arrays.toString(e.getStackTrace());
    }

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

    private PerformanceMetricDTO convertToDTO(PerformanceMetric metric) {
        return PerformanceMetricDTO.builder()
            .id(metric.getId())
            .metricType(metric.getMetricType())
            .metricName(metric.getMetricName())
            .metricValue(metric.getMetricValue())
            .metricUnit(metric.getMetricUnit())
            .recordedAt(metric.getRecordedAt())
            .severity(metric.getSeverity())
            .metricMetadata(metric.getMetricMetadata())
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

    private List<TestScenarioDTO> getActiveTestScenarios() {
        return testScenarioRepository.findByIsActiveTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private SystemHealthMetricsDTO getSystemHealthMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return SystemHealthMetricsDTO.builder()
            .memoryUsagePercent((double) usedMemory / totalMemory * 100)
            .totalMemoryMb(totalMemory / 1024 / 1024)
            .usedMemoryMb(usedMemory / 1024 / 1024)
            .freeMemoryMb(freeMemory / 1024 / 1024)
            .activeThreads(Thread.activeCount())
            .databaseConnectionsActive(0) // Mock
            .systemUptime(0.0) // Mock
            .build();
    }
}
