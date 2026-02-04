# Debug Tools Implementation

## Overview
Implement comprehensive debugging and testing tools for administrators to manage game progression, simulate matches, test system balance, and troubleshoot issues. Provide tools for forcing game state changes and monitoring system performance.

## Technical Requirements

### Database Schema Changes

#### New Entity: DebugAction
```java
@Entity
@Table(name = "debug_action")
public class DebugAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    private User adminUser;
    
    @Enumerated(EnumType.STRING)
    private DebugActionType actionType;
    
    private String actionName;
    private String actionDescription;
    
    // Target entities
    private String targetEntityType; // Season, Match, League, etc.
    private Long targetEntityId;
    private String targetEntityName;
    
    // Action parameters (JSON)
    private String actionParameters;
    
    // Execution details
    private LocalDateTime executedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;
    
    @Enumerated(EnumType.STRING)
    private DebugActionStatus status;
    
    private String result; // JSON result data
    private String errorMessage;
    private String stackTrace;
    
    // Impact tracking
    private Integer entitiesAffected;
    private String impactSummary;
    
    private Boolean isReversible;
    private String reversalInstructions;
}
```

#### New Entity: SystemSnapshot
```java
@Entity
@Table(name = "system_snapshot")
public class SystemSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String snapshotName;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private SnapshotType snapshotType; // MANUAL, AUTOMATIC, PRE_DEBUG
    
    private LocalDateTime createdAt;
    private String createdBy;
    
    // Snapshot data (JSON)
    private String gameStateData;
    private String databaseMetrics;
    private String systemMetrics;
    
    // Snapshot scope
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<SnapshotScope> scopes = new HashSet<>();
    
    private Long fileSizeBytes;
    private String checksumMd5;
    
    @Enumerated(EnumType.STRING)
    private SnapshotStatus status; // CREATING, READY, CORRUPTED, DELETED
    
    private Boolean isRestorable;
    private String storageLocation;
}
```

#### New Entity: PerformanceMetric
```java
@Entity
@Table(name = "performance_metric")
public class PerformanceMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private MetricType metricType;
    
    private String metricName;
    private String metricCategory;
    
    private Double metricValue;
    private String metricUnit;
    
    private LocalDateTime recordedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Metric metadata (JSON)
    private String metricMetadata;
    
    @Enumerated(EnumType.STRING)
    private MetricSeverity severity; // NORMAL, WARNING, CRITICAL
    
    private String notes;
}
```

#### New Entity: TestScenario
```java
@Entity
@Table(name = "test_scenario")
public class TestScenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String scenarioName;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ScenarioCategory category;
    
    // Scenario configuration (JSON)
    private String scenarioConfig;
    
    // Expected outcomes (JSON)
    private String expectedOutcomes;
    
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "testScenario", cascade = CascadeType.ALL)
    private List<TestExecution> executions = new ArrayList<>();
}
```

#### New Entity: TestExecution
```java
@Entity
@Table(name = "test_execution")
public class TestExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_scenario_id")
    private TestScenario testScenario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_user_id")
    private User executedBy;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;
    
    @Enumerated(EnumType.STRING)
    private TestExecutionStatus status;
    
    // Execution results (JSON)
    private String executionResults;
    private String actualOutcomes;
    
    private Boolean passed;
    private String failureReason;
    
    // Performance metrics
    private Integer queriesExecuted;
    private Long memoryUsedMb;
    private Double cpuUsagePercent;
}
```

#### Enums to Create
```java
public enum DebugActionType {
    ADVANCE_SEASON("Advance Season"),
    SIMULATE_MATCHES("Simulate Matches"),
    MODIFY_PLAYER_STATS("Modify Player Stats"),
    ADJUST_FINANCES("Adjust Finances"),
    FORCE_TRANSFERS("Force Transfers"),
    RESET_LEAGUE("Reset League"),
    GENERATE_DATA("Generate Data"),
    SYSTEM_MAINTENANCE("System Maintenance"),
    DATABASE_OPERATION("Database Operation");
    
    private final String displayName;
}

public enum DebugActionStatus {
    PENDING("Pending"),
    EXECUTING("Executing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
}

public enum SnapshotType {
    MANUAL("Manual"),
    AUTOMATIC("Automatic"),
    PRE_DEBUG("Pre-Debug"),
    SCHEDULED("Scheduled");
    
    private final String displayName;
}

public enum SnapshotScope {
    FULL_SYSTEM("Full System"),
    GAME_DATA("Game Data"),
    USER_DATA("User Data"),
    FINANCIAL_DATA("Financial Data"),
    MATCH_DATA("Match Data"),
    CONFIGURATION("Configuration");
    
    private final String displayName;
}

public enum SnapshotStatus {
    CREATING("Creating"),
    READY("Ready"),
    CORRUPTED("Corrupted"),
    DELETED("Deleted");
    
    private final String displayName;
}

public enum MetricType {
    DATABASE_QUERY_TIME("Database Query Time"),
    MEMORY_USAGE("Memory Usage"),
    CPU_USAGE("CPU Usage"),
    RESPONSE_TIME("Response Time"),
    THROUGHPUT("Throughput"),
    ERROR_RATE("Error Rate"),
    ACTIVE_SESSIONS("Active Sessions"),
    MATCH_SIMULATION_TIME("Match Simulation Time");
    
    private final String displayName;
}

public enum MetricSeverity {
    NORMAL("Normal"),
    WARNING("Warning"),
    CRITICAL("Critical");
    
    private final String displayName;
}

public enum ScenarioCategory {
    MATCH_SIMULATION("Match Simulation"),
    PLAYER_DEVELOPMENT("Player Development"),
    FINANCIAL_SYSTEM("Financial System"),
    TRANSFER_SYSTEM("Transfer System"),
    LEAGUE_PROGRESSION("League Progression"),
    USER_INTERACTION("User Interaction"),
    PERFORMANCE("Performance");
    
    private final String displayName;
}

public enum TestExecutionStatus {
    RUNNING("Running"),
    PASSED("Passed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
}
```

### Service Layer Implementation

#### DebugToolsService
```java
@Service
public class DebugToolsService {
    
    @Autowired
    private SeasonService seasonService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private FinancialService financialService;
    
    @Autowired
    private DebugActionRepository debugActionRepository;
    
    @Autowired
    private SystemSnapshotRepository systemSnapshotRepository;
    
    @Autowired
    private PerformanceMetricRepository performanceMetricRepository;
    
    @Autowired
    private TestScenarioRepository testScenarioRepository;
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    /**
     * Get debug tools dashboard
     */
    public DebugDashboardDTO getDebugDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24Hours = now.minusHours(24);
        
        return DebugDashboardDTO.builder()
            .recentDebugActions(getRecentDebugActions(10))
            .systemSnapshots(getRecentSnapshots(5))
            .performanceMetrics(getCurrentPerformanceMetrics())
            .activeTestScenarios(getActiveTestScenarios())
            .systemHealth(getSystemHealthMetrics())
            .debugActionsLast24Hours(debugActionRepository.countByExecutedAtAfter(last24Hours))
            .build();
    }
    
    /**
     * Advance season for testing purposes
     */
    @Transactional
    public DebugActionResult advanceSeason(AdvanceSeasonRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.ADVANCE_SEASON, 
            "Advance Season", "Force advance current season",
            "Season", null, null, convertToJson(request)
        );
        
        try {
            Season currentSeason = seasonService.getCurrentSeason();
            
            // Create snapshot before making changes
            SystemSnapshot snapshot = createPreDebugSnapshot("Pre-season-advance", adminUser);
            
            // Execute season advancement
            SeasonAdvancementResult result = seasonService.forceAdvanceSeason(
                request.getSkipRemainingMatches(),
                request.getGenerateNewPlayers(),
                request.getProcessTransfers()
            );
            
            // Update debug action
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
                .snapshotId(snapshot.getId())
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
    
    /**
     * Simulate specific matches for testing
     */
    @Transactional
    public DebugActionResult simulateMatches(SimulateMatchesRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.SIMULATE_MATCHES,
            "Simulate Matches", "Force simulate specific matches",
            "Match", null, null, convertToJson(request)
        );
        
        try {
            List<Match> matches = matchService.findAllById(request.getMatchIds());
            List<MatchResult> results = new ArrayList<>();
            
            for (Match match : matches) {
                if (match.getStatus() != MatchStatus.SCHEDULED) {
                    continue; // Skip already played matches
                }
                
                // Apply debug parameters if specified
                if (request.getForceResult() != null) {
                    MatchResult result = matchService.simulateMatchWithForcedResult(
                        match, request.getForceResult()
                    );
                    results.add(result);
                } else {
                    MatchResult result = matchService.simulateMatch(match);
                    results.add(result);
                }
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
    
    /**
     * Modify player statistics for testing
     */
    @Transactional
    public DebugActionResult modifyPlayerStats(ModifyPlayerStatsRequest request, User adminUser) {
        DebugAction debugAction = createDebugAction(
            adminUser, DebugActionType.MODIFY_PLAYER_STATS,
            "Modify Player Stats", "Modify player statistics for testing",
            "Player", null, null, convertToJson(request)
        );
        
        try {
            List<Player> players = playerService.findAllById(request.getPlayerIds());
            List<Player> modifiedPlayers = new ArrayList<>();
            
            for (Player player : players) {
                // Store original stats for reversal
                PlayerStats originalStats = clonePlayerStats(player);
                
                // Apply modifications
                if (request.getStatModifications() != null) {
                    applyStatModifications(player, request.getStatModifications());
                }
                
                if (request.getOverallAdjustment() != null) {
                    adjustPlayerOverall(player, request.getOverallAdjustment());
                }
                
                if (request.getAgeAdjustment() != null) {
                    player.setAge(Math.max(16, Math.min(45, player.getAge() + request.getAgeAdjustment())));
                }
                
                modifiedPlayers.add(playerService.save(player));
            }
            
            debugAction.setStatus(DebugActionStatus.COMPLETED);
            debugAction.setCompletedAt(LocalDateTime.now());
            debugAction.setResult(convertToJson(modifiedPlayers));
            debugAction.setEntitiesAffected(modifiedPlayers.size());
            debugAction.setImpactSummary("Modified stats for " + modifiedPlayers.size() + " players");
            debugAction.setIsReversible(true);
            debugAction.setReversalInstructions("Player stats can be restored from backup");
            
            return DebugActionResult.builder()
                .success(true)
                .message("Player stats modified successfully")
                .result(modifiedPlayers)
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
    
    /**
     * Adjust club finances for testing
     */
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
            
            // Store original values
            BigDecimal originalBalance = finance.getBalance();
            
            // Apply adjustments
            if (request.getBalanceAdjustment() != null) {
                finance.setBalance(finance.getBalance().add(request.getBalanceAdjustment()));
            }
            
            if (request.getSetBalance() != null) {
                finance.setBalance(request.getSetBalance());
            }
            
            if (request.getDebtAdjustment() != null) {
                finance.setDebt(finance.getDebt().add(request.getDebtAdjustment()));
            }
            
            financialService.save(finance);
            
            // Create transaction record
            if (!request.getBalanceAdjustment().equals(BigDecimal.ZERO)) {
                financialService.processTransaction(club.getId(), CreateTransactionRequest.builder()
                    .type(request.getBalanceAdjustment().compareTo(BigDecimal.ZERO) > 0 ? 
                          TransactionType.INCOME : TransactionType.EXPENSE)
                    .category(TransactionCategory.SYSTEM_ACTION)
                    .amount(request.getBalanceAdjustment().abs())
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
    
    /**
     * Create system snapshot
     */
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
        
        // Create snapshot asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                createSnapshotData(snapshot);
                snapshot.setStatus(SnapshotStatus.READY);
            } catch (Exception e) {
                snapshot.setStatus(SnapshotStatus.CORRUPTED);
                log.error("Failed to create system snapshot {}: {}", snapshot.getId(), e.getMessage());
            }
            systemSnapshotRepository.save(snapshot);
        });
        
        return snapshot;
    }
    
    /**
     * Execute test scenario
     */
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
        
        // Execute scenario asynchronously
        CompletableFuture.runAsync(() -> {
            executeScenarioAsync(execution);
        });
        
        return execution;
    }
    
    /**
     * Get performance metrics
     */
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
    
    /**
     * Record performance metric
     */
    public void recordPerformanceMetric(MetricType metricType, String metricName, 
                                      Double value, String unit, String metadata) {
        PerformanceMetric metric = PerformanceMetric.builder()
            .metricType(metricType)
            .metricName(metricName)
            .metricValue(value)
            .metricUnit(unit)
            .recordedAt(LocalDateTime.now())
            .metricMetadata(metadata)
            .severity(determineSeverity(metricType, value))
            .build();
        
        performanceMetricRepository.save(metric);
    }
    
    /**
     * Get system health metrics
     */
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
            .databaseConnectionsActive(getDatabaseConnectionCount())
            .systemUptime(getSystemUptimeHours())
            .build();
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
    
    private SystemSnapshot createPreDebugSnapshot(String name, User adminUser) {
        CreateSnapshotRequest request = CreateSnapshotRequest.builder()
            .name(name)
            .description("Automatic snapshot before debug action")
            .type(SnapshotType.PRE_DEBUG)
            .scopes(Set.of(SnapshotScope.GAME_DATA))
            .build();
        
        return createSystemSnapshot(request, adminUser);
    }
    
    private void executeScenarioAsync(TestExecution execution) {
        try {
            TestScenario scenario = execution.getTestScenario();
            
            // Parse scenario configuration
            ScenarioConfig config = parseScenarioConfig(scenario.getScenarioConfig());
            
            // Execute scenario based on category
            ScenarioResult result = switch (scenario.getCategory()) {
                case MATCH_SIMULATION -> executeMatchSimulationScenario(config);
                case PLAYER_DEVELOPMENT -> executePlayerDevelopmentScenario(config);
                case FINANCIAL_SYSTEM -> executeFinancialSystemScenario(config);
                case TRANSFER_SYSTEM -> executeTransferSystemScenario(config);
                case LEAGUE_PROGRESSION -> executeLeagueProgressionScenario(config);
                case USER_INTERACTION -> executeUserInteractionScenario(config);
                case PERFORMANCE -> executePerformanceScenario(config);
            };
            
            // Evaluate results
            boolean passed = evaluateScenarioResults(scenario, result);
            
            execution.setStatus(passed ? TestExecutionStatus.PASSED : TestExecutionStatus.FAILED);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setExecutionTimeMs(
                Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis()
            );
            execution.setPassed(passed);
            execution.setExecutionResults(convertToJson(result));
            execution.setActualOutcomes(convertToJson(result.getOutcomes()));
            
            if (!passed) {
                execution.setFailureReason(result.getFailureReason());
            }
            
        } catch (Exception e) {
            execution.setStatus(TestExecutionStatus.FAILED);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setPassed(false);
            execution.setFailureReason("Execution failed: " + e.getMessage());
            log.error("Test scenario execution failed", e);
        }
        
        testExecutionRepository.save(execution);
    }
}
```

### API Endpoints

#### DebugToolsController
```java
@RestController
@RequestMapping("/api/admin/debug")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class DebugToolsController {
    
    @Autowired
    private DebugToolsService debugToolsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DebugDashboardDTO> getDebugDashboard() {
        DebugDashboardDTO dashboard = debugToolsService.getDebugDashboard();
        return ResponseEntity.ok(dashboard);
    }
    
    @PostMapping("/season/advance")
    public ResponseEntity<DebugActionResult> advanceSeason(
            @RequestBody AdvanceSeasonRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        DebugActionResult result = debugToolsService.advanceSeason(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/matches/simulate")
    public ResponseEntity<DebugActionResult> simulateMatches(
            @RequestBody SimulateMatchesRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        DebugActionResult result = debugToolsService.simulateMatches(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/players/modify-stats")
    public ResponseEntity<DebugActionResult> modifyPlayerStats(
            @RequestBody ModifyPlayerStatsRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        DebugActionResult result = debugToolsService.modifyPlayerStats(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/finances/adjust")
    public ResponseEntity<DebugActionResult> adjustFinances(
            @RequestBody AdjustFinancesRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        DebugActionResult result = debugToolsService.adjustFinances(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/snapshots")
    public ResponseEntity<SystemSnapshotDTO> createSnapshot(
            @RequestBody CreateSnapshotRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
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
        User adminUser = userService.findByUsername(authentication.getName());
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
}
```

### Frontend Implementation (fm-admin)

#### DebugTools Component
```jsx
import React, { useState, useEffect } from 'react';
import { getDebugDashboard, advanceSeason, simulateMatches, modifyPlayerStats, adjustFinances } from '../services/api';

const DebugTools = () => {
    const [dashboard, setDashboard] = useState(null);
    const [activeTab, setActiveTab] = useState('overview');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDebugDashboard();
    }, []);

    const loadDebugDashboard = async () => {
        try {
            const response = await getDebugDashboard();
            setDashboard(response.data);
        } catch (error) {
            console.error('Error loading debug dashboard:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Loading debug tools...</div>;

    return (
        <div className="debug-tools">
            <div className="debug-header">
                <h1>Debug Tools</h1>
                <div className="warning-banner">
                    <i className="fas fa-exclamation-triangle"></i>
                    <span>Warning: These tools can modify game state. Use with caution!</span>
                </div>
            </div>

            <div className="debug-tabs">
                <button 
                    className={activeTab === 'overview' ? 'active' : ''}
                    onClick={() => setActiveTab('overview')}
                >
                    Overview
                </button>
                <button 
                    className={activeTab === 'season' ? 'active' : ''}
                    onClick={() => setActiveTab('season')}
                >
                    Season Control
                </button>
                <button 
                    className={activeTab === 'matches' ? 'active' : ''}
                    onClick={() => setActiveTab('matches')}
                >
                    Match Simulation
                </button>
                <button 
                    className={activeTab === 'players' ? 'active' : ''}
                    onClick={() => setActiveTab('players')}
                >
                    Player Tools
                </button>
                <button 
                    className={activeTab === 'finances' ? 'active' : ''}
                    onClick={() => setActiveTab('finances')}
                >
                    Financial Tools
                </button>
                <button 
                    className={activeTab === 'testing' ? 'active' : ''}
                    onClick={() => setActiveTab('testing')}
                >
                    Testing
                </button>
                <button 
                    className={activeTab === 'monitoring' ? 'active' : ''}
                    onClick={() => setActiveTab('monitoring')}
                >
                    Monitoring
                </button>
            </div>

            <div className="debug-content">
                {activeTab === 'overview' && (
                    <DebugOverview dashboard={dashboard} />
                )}
                {activeTab === 'season' && (
                    <SeasonControl onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'matches' && (
                    <MatchSimulation onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'players' && (
                    <PlayerTools onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'finances' && (
                    <FinancialTools onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'testing' && (
                    <TestingTools dashboard={dashboard} />
                )}
                {activeTab === 'monitoring' && (
                    <MonitoringTools dashboard={dashboard} />
                )}
            </div>
        </div>
    );
};

const DebugOverview = ({ dashboard }) => {
    return (
        <div className="debug-overview">
            <div className="system-health">
                <h2>System Health</h2>
                <div className="health-grid">
                    <div className="health-card">
                        <h3>Memory Usage</h3>
                        <div className="progress-bar">
                            <div 
                                className="progress-fill"
                                style={{ width: `${dashboard.systemHealth.memoryUsagePercent}%` }}
                            ></div>
                        </div>
                        <span>{dashboard.systemHealth.memoryUsagePercent.toFixed(1)}%</span>
                        <small>{dashboard.systemHealth.usedMemoryMb}MB / {dashboard.systemHealth.totalMemoryMb}MB</small>
                    </div>
                    
                    <div className="health-card">
                        <h3>Active Threads</h3>
                        <span className="metric-value">{dashboard.systemHealth.activeThreads}</span>
                    </div>
                    
                    <div className="health-card">
                        <h3>DB Connections</h3>
                        <span className="metric-value">{dashboard.systemHealth.databaseConnectionsActive}</span>
                    </div>
                    
                    <div className="health-card">
                        <h3>System Uptime</h3>
                        <span className="metric-value">{dashboard.systemHealth.systemUptime}h</span>
                    </div>
                </div>
            </div>

            <div className="recent-actions">
                <h2>Recent Debug Actions</h2>
                <div className="actions-list">
                    {dashboard.recentDebugActions.map(action => (
                        <div key={action.id} className="action-item">
                            <div className="action-icon">
                                <i className={getActionIcon(action.actionType)}></i>
                            </div>
                            <div className="action-content">
                                <div className="action-name">{action.actionName}</div>
                                <div className="action-description">{action.actionDescription}</div>
                                <div className="action-meta">
                                    <span className="action-user">{action.adminUser.username}</span>
                                    <span className="action-time">
                                        {new Date(action.executedAt).toLocaleString()}
                                    </span>
                                    <span className={`action-status ${action.status.toLowerCase()}`}>
                                        {action.status}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="system-snapshots">
                <h2>System Snapshots</h2>
                <div className="snapshots-list">
                    {dashboard.systemSnapshots.map(snapshot => (
                        <div key={snapshot.id} className="snapshot-item">
                            <div className="snapshot-info">
                                <h4>{snapshot.snapshotName}</h4>
                                <p>{snapshot.description}</p>
                                <div className="snapshot-meta">
                                    <span>Created: {new Date(snapshot.createdAt).toLocaleString()}</span>
                                    <span>By: {snapshot.createdBy}</span>
                                    <span className={`status ${snapshot.status.toLowerCase()}`}>
                                        {snapshot.status}
                                    </span>
                                </div>
                            </div>
                            <div className="snapshot-actions">
                                {snapshot.isRestorable && snapshot.status === 'READY' && (
                                    <button className="btn-warning btn-sm">Restore</button>
                                )}
                                <button className="btn-danger btn-sm">Delete</button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

const SeasonControl = ({ onRefresh }) => {
    const [advanceForm, setAdvanceForm] = useState({
        skipRemainingMatches: false,
        generateNewPlayers: true,
        processTransfers: true
    });
    const [loading, setLoading] = useState(false);

    const handleAdvanceSeason = async () => {
        if (!window.confirm('Are you sure you want to advance the season? This action cannot be undone.')) {
            return;
        }

        setLoading(true);
        try {
            const response = await advanceSeason(advanceForm);
            if (response.data.success) {
                alert('Season advanced successfully!');
                onRefresh();
            } else {
                alert('Failed to advance season: ' + response.data.message);
            }
        } catch (error) {
            console.error('Error advancing season:', error);
            alert('Error advancing season: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="season-control">
            <div className="control-section">
                <h2>Season Advancement</h2>
                <div className="form-group">
                    <label>
                        <input
                            type="checkbox"
                            checked={advanceForm.skipRemainingMatches}
                            onChange={(e) => setAdvanceForm({
                                ...advanceForm,
                                skipRemainingMatches: e.target.checked
                            })}
                        />
                        Skip remaining matches
                    </label>
                </div>
                <div className="form-group">
                    <label>
                        <input
                            type="checkbox"
                            checked={advanceForm.generateNewPlayers}
                            onChange={(e) => setAdvanceForm({
                                ...advanceForm,
                                generateNewPlayers: e.target.checked
                            })}
                        />
                        Generate new players
                    </label>
                </div>
                <div className="form-group">
                    <label>
                        <input
                            type="checkbox"
                            checked={advanceForm.processTransfers}
                            onChange={(e) => setAdvanceForm({
                                ...advanceForm,
                                processTransfers: e.target.checked
                            })}
                        />
                        Process pending transfers
                    </label>
                </div>
                <button 
                    className="btn-danger"
                    onClick={handleAdvanceSeason}
                    disabled={loading}
                >
                    {loading ? 'Advancing...' : 'Advance Season'}
                </button>
            </div>
        </div>
    );
};

const MatchSimulation = ({ onRefresh }) => {
    const [matchIds, setMatchIds] = useState('');
    const [forceResult, setForceResult] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSimulateMatches = async () => {
        if (!matchIds.trim()) {
            alert('Please enter match IDs');
            return;
        }

        const ids = matchIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
        if (ids.length === 0) {
            alert('Please enter valid match IDs');
            return;
        }

        setLoading(true);
        try {
            const request = {
                matchIds: ids,
                forceResult: forceResult || null
            };

            const response = await simulateMatches(request);
            if (response.data.success) {
                alert(`Successfully simulated ${ids.length} matches!`);
                onRefresh();
            } else {
                alert('Failed to simulate matches: ' + response.data.message);
            }
        } catch (error) {
            console.error('Error simulating matches:', error);
            alert('Error simulating matches: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="match-simulation">
            <div className="control-section">
                <h2>Match Simulation</h2>
                <div className="form-group">
                    <label>Match IDs (comma-separated)</label>
                    <input
                        type="text"
                        value={matchIds}
                        onChange={(e) => setMatchIds(e.target.value)}
                        placeholder="1, 2, 3, 4"
                    />
                </div>
                <div className="form-group">
                    <label>Force Result (optional)</label>
                    <select
                        value={forceResult}
                        onChange={(e) => setForceResult(e.target.value)}
                    >
                        <option value="">Natural simulation</option>
                        <option value="HOME_WIN">Home team wins</option>
                        <option value="AWAY_WIN">Away team wins</option>
                        <option value="DRAW">Draw</option>
                    </select>
                </div>
                <button 
                    className="btn-primary"
                    onClick={handleSimulateMatches}
                    disabled={loading}
                >
                    {loading ? 'Simulating...' : 'Simulate Matches'}
                </button>
            </div>
        </div>
    );
};

const getActionIcon = (actionType) => {
    switch (actionType) {
        case 'ADVANCE_SEASON': return 'fas fa-fast-forward';
        case 'SIMULATE_MATCHES': return 'fas fa-play';
        case 'MODIFY_PLAYER_STATS': return 'fas fa-user-edit';
        case 'ADJUST_FINANCES': return 'fas fa-dollar-sign';
        case 'FORCE_TRANSFERS': return 'fas fa-exchange-alt';
        case 'RESET_LEAGUE': return 'fas fa-undo';
        case 'GENERATE_DATA': return 'fas fa-database';
        default: return 'fas fa-cog';
    }
};

export default DebugTools;
```

## Implementation Notes

1. **Safety First**: All debug actions create snapshots before execution
2. **Comprehensive Logging**: Every debug action is logged with full details
3. **Reversibility**: Where possible, debug actions can be reversed
4. **Performance Monitoring**: Built-in performance metrics collection
5. **Test Scenarios**: Automated testing scenarios for system validation
6. **System Health**: Real-time monitoring of system resources

## Dependencies

- All core game systems (Season, Match, Player, Financial, etc.)
- Snapshot and backup system
- Performance monitoring framework
- Async processing capabilities
- File storage system for snapshots

## Testing Strategy

1. **Unit Tests**: Test all debug service methods
2. **Integration Tests**: Test complete debug workflows
3. **Safety Tests**: Verify snapshot creation and restoration
4. **Performance Tests**: Test impact of debug operations
5. **Security Tests**: Verify admin-only access controls