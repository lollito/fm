# Admin Panel Enhancement Implementation

## Overview
Enhance the existing fm-admin React application with comprehensive administrative tools for managing leagues, clubs, players, and game world settings. Provide intuitive interfaces for creating, modifying, and deleting game entities.

## Technical Requirements

### Database Schema Changes

#### New Entity: AdminAction
```java
@Entity
@Table(name = "admin_action")
public class AdminAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    private User adminUser;
    
    @Enumerated(EnumType.STRING)
    private AdminActionType actionType;
    
    private String entityType; // Club, Player, League, etc.
    private Long entityId;
    private String entityName;
    
    private String actionDescription;
    private String oldValues; // JSON of previous values
    private String newValues; // JSON of new values
    
    private LocalDateTime actionTimestamp;
    private String ipAddress;
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    private ActionStatus status; // SUCCESS, FAILED, PENDING
    
    private String failureReason;
    private String notes;
}
```

#### New Entity: SystemConfiguration
```java
@Entity
@Table(name = "system_configuration")
public class SystemConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String configKey;
    private String configValue;
    private String configDescription;
    
    @Enumerated(EnumType.STRING)
    private ConfigCategory category;
    
    @Enumerated(EnumType.STRING)
    private ConfigType valueType; // STRING, INTEGER, BOOLEAN, DECIMAL
    
    private String defaultValue;
    private Boolean isEditable;
    private Boolean requiresRestart;
    
    private LocalDateTime lastModified;
    private String modifiedBy;
    
    private String validationRules; // JSON for validation
}
```

#### Enhanced User Entity for Admin Roles
```java
// Add to existing User entity
@Enumerated(EnumType.STRING)
private AdminRole adminRole; // SUPER_ADMIN, LEAGUE_ADMIN, MODERATOR

private Boolean isActive;
private LocalDateTime lastLoginDate;
private Integer failedLoginAttempts;
private LocalDateTime accountLockedUntil;

@OneToMany(mappedBy = "adminUser", cascade = CascadeType.ALL)
private List<AdminAction> adminActions = new ArrayList<>();
```

#### Enums to Create
```java
public enum AdminActionType {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    ACTIVATE("Activate"),
    DEACTIVATE("Deactivate"),
    RESET("Reset"),
    BULK_UPDATE("Bulk Update"),
    IMPORT("Import"),
    EXPORT("Export");
    
    private final String displayName;
}

public enum ActionStatus {
    SUCCESS("Success"),
    FAILED("Failed"),
    PENDING("Pending");
    
    private final String displayName;
}

public enum ConfigCategory {
    GAME_SETTINGS("Game Settings"),
    MATCH_SIMULATION("Match Simulation"),
    PLAYER_GENERATION("Player Generation"),
    FINANCIAL("Financial"),
    SECURITY("Security"),
    PERFORMANCE("Performance");
    
    private final String displayName;
}

public enum ConfigType {
    STRING("String"),
    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    DECIMAL("Decimal"),
    JSON("JSON");
    
    private final String displayName;
}

public enum AdminRole {
    SUPER_ADMIN("Super Administrator"),
    LEAGUE_ADMIN("League Administrator"),
    MODERATOR("Moderator");
    
    private final String displayName;
}
```

### Service Layer Implementation

#### AdminService
```java
@Service
public class AdminService {
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private LeagueRepository leagueRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminActionRepository adminActionRepository;
    
    @Autowired
    private SystemConfigurationRepository systemConfigRepository;
    
    /**
     * Get admin dashboard statistics
     */
    public AdminDashboardDTO getAdminDashboard() {
        return AdminDashboardDTO.builder()
            .totalClubs(clubRepository.count())
            .totalPlayers(playerRepository.count())
            .totalUsers(userRepository.count())
            .totalLeagues(leagueRepository.count())
            .activeUsers(userRepository.countByIsActive(true))
            .recentActions(getRecentAdminActions(10))
            .systemHealth(getSystemHealthStatus())
            .build();
    }
    
    /**
     * Create new club with complete setup
     */
    @Transactional
    public Club createClub(CreateClubRequest request, User adminUser) {
        // Validate request
        validateClubCreationRequest(request);
        
        // Create club
        Club club = Club.builder()
            .name(request.getName())
            .shortName(request.getShortName())
            .foundedYear(request.getFoundedYear())
            .city(request.getCity())
            .country(countryService.findById(request.getCountryId()))
            .league(leagueService.findById(request.getLeagueId()))
            .build();
        
        club = clubRepository.save(club);
        
        // Create stadium
        if (request.getStadiumRequest() != null) {
            Stadium stadium = createStadiumForClub(club, request.getStadiumRequest());
            club.setStadium(stadium);
        }
        
        // Create team
        Team team = createTeamForClub(club);
        club.setTeam(team);
        
        // Create finance
        Finance finance = createFinanceForClub(club, request.getInitialBudget());
        club.setFinance(finance);
        
        // Generate initial players if requested
        if (request.getGenerateInitialSquad()) {
            generateInitialSquad(team, request.getSquadQuality());
        }
        
        club = clubRepository.save(club);
        
        // Log admin action
        logAdminAction(adminUser, AdminActionType.CREATE, "Club", club.getId(), 
                      club.getName(), null, convertToJson(club));
        
        return club;
    }
    
    /**
     * Bulk update players
     */
    @Transactional
    public BulkUpdateResult bulkUpdatePlayers(BulkUpdatePlayersRequest request, User adminUser) {
        List<Player> players = playerRepository.findAllById(request.getPlayerIds());
        List<Player> updatedPlayers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Player player : players) {
            try {
                // Apply updates based on request
                if (request.getSalaryMultiplier() != null) {
                    BigDecimal newSalary = player.getSalary()
                        .multiply(request.getSalaryMultiplier());
                    player.setSalary(newSalary);
                }
                
                if (request.getSkillAdjustment() != null) {
                    adjustPlayerSkills(player, request.getSkillAdjustment());
                }
                
                if (request.getNewClubId() != null) {
                    Club newClub = clubService.findById(request.getNewClubId());
                    transferPlayerToClub(player, newClub);
                }
                
                updatedPlayers.add(playerRepository.save(player));
                
            } catch (Exception e) {
                errors.add("Failed to update player " + player.getName() + ": " + e.getMessage());
            }
        }
        
        // Log bulk action
        logAdminAction(adminUser, AdminActionType.BULK_UPDATE, "Player", null, 
                      "Bulk update of " + request.getPlayerIds().size() + " players",
                      null, convertToJson(request));
        
        return BulkUpdateResult.builder()
            .totalRequested(request.getPlayerIds().size())
            .successfulUpdates(updatedPlayers.size())
            .failedUpdates(errors.size())
            .updatedEntities(updatedPlayers.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .errors(errors)
            .build();
    }
    
    /**
     * Generate players for league
     */
    @Transactional
    public PlayerGenerationResult generatePlayersForLeague(Long leagueId, 
                                                          PlayerGenerationRequest request, 
                                                          User adminUser) {
        League league = leagueService.findById(leagueId);
        List<Club> clubs = league.getClubs();
        List<Player> generatedPlayers = new ArrayList<>();
        
        for (Club club : clubs) {
            Team team = club.getTeam();
            int currentSquadSize = team.getPlayers().size();
            int playersNeeded = Math.max(0, request.getTargetSquadSize() - currentSquadSize);
            
            if (playersNeeded > 0) {
                List<Player> newPlayers = generatePlayersForTeam(team, playersNeeded, 
                                                               request.getQualityRange());
                generatedPlayers.addAll(newPlayers);
            }
        }
        
        // Log admin action
        logAdminAction(adminUser, AdminActionType.CREATE, "Player", null, 
                      "Generated " + generatedPlayers.size() + " players for league " + league.getName(),
                      null, convertToJson(request));
        
        return PlayerGenerationResult.builder()
            .totalGenerated(generatedPlayers.size())
            .playersPerClub(generatedPlayers.size() / clubs.size())
            .generatedPlayers(generatedPlayers.stream().map(this::convertToDTO).collect(Collectors.toList()))
            .build();
    }
    
    /**
     * Import data from CSV/JSON
     */
    @Transactional
    public ImportResult importData(ImportDataRequest request, User adminUser) {
        ImportResult result = ImportResult.builder()
            .totalRecords(0)
            .successfulImports(0)
            .failedImports(0)
            .errors(new ArrayList<>())
            .build();
        
        try {
            switch (request.getDataType()) {
                case PLAYERS -> result = importPlayers(request.getData(), request.getOptions());
                case CLUBS -> result = importClubs(request.getData(), request.getOptions());
                case LEAGUES -> result = importLeagues(request.getData(), request.getOptions());
                default -> throw new IllegalArgumentException("Unsupported data type: " + request.getDataType());
            }
            
            // Log import action
            logAdminAction(adminUser, AdminActionType.IMPORT, request.getDataType().toString(), null,
                          "Imported " + result.getSuccessfulImports() + " records",
                          null, convertToJson(request));
            
        } catch (Exception e) {
            result.getErrors().add("Import failed: " + e.getMessage());
            logAdminAction(adminUser, AdminActionType.IMPORT, request.getDataType().toString(), null,
                          "Import failed", null, e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get system configuration
     */
    public List<SystemConfigurationDTO> getSystemConfiguration(ConfigCategory category) {
        List<SystemConfiguration> configs;
        
        if (category != null) {
            configs = systemConfigRepository.findByCategory(category);
        } else {
            configs = systemConfigRepository.findAll();
        }
        
        return configs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update system configuration
     */
    @Transactional
    public SystemConfiguration updateSystemConfiguration(Long configId, 
                                                        String newValue, 
                                                        User adminUser) {
        SystemConfiguration config = systemConfigRepository.findById(configId)
            .orElseThrow(() -> new EntityNotFoundException("Configuration not found"));
        
        if (!config.getIsEditable()) {
            throw new IllegalStateException("Configuration is not editable");
        }
        
        // Validate new value
        validateConfigValue(config, newValue);
        
        String oldValue = config.getConfigValue();
        config.setConfigValue(newValue);
        config.setLastModified(LocalDateTime.now());
        config.setModifiedBy(adminUser.getUsername());
        
        config = systemConfigRepository.save(config);
        
        // Log configuration change
        logAdminAction(adminUser, AdminActionType.UPDATE, "SystemConfiguration", 
                      config.getId(), config.getConfigKey(), oldValue, newValue);
        
        // Apply configuration change if needed
        applyConfigurationChange(config);
        
        return config;
    }
    
    /**
     * Get admin action history
     */
    public Page<AdminActionDTO> getAdminActionHistory(AdminActionFilter filter, Pageable pageable) {
        Specification<AdminAction> spec = buildAdminActionSpecification(filter);
        Page<AdminAction> actions = adminActionRepository.findAll(spec, pageable);
        
        return actions.map(this::convertToDTO);
    }
    
    /**
     * Export data to various formats
     */
    public ExportResult exportData(ExportDataRequest request, User adminUser) {
        ExportResult result;
        
        try {
            switch (request.getDataType()) {
                case PLAYERS -> result = exportPlayers(request);
                case CLUBS -> result = exportClubs(request);
                case LEAGUES -> result = exportLeagues(request);
                case MATCHES -> result = exportMatches(request);
                case USERS -> result = exportUsers(request);
                default -> throw new IllegalArgumentException("Unsupported export type: " + request.getDataType());
            }
            
            // Log export action
            logAdminAction(adminUser, AdminActionType.EXPORT, request.getDataType().toString(), null,
                          "Exported " + result.getRecordCount() + " records",
                          null, convertToJson(request));
            
        } catch (Exception e) {
            result = ExportResult.builder()
                .success(false)
                .errorMessage("Export failed: " + e.getMessage())
                .build();
        }
        
        return result;
    }
    
    /**
     * Get system health status
     */
    private SystemHealthDTO getSystemHealthStatus() {
        return SystemHealthDTO.builder()
            .databaseStatus(checkDatabaseHealth())
            .memoryUsage(getMemoryUsage())
            .activeConnections(getActiveConnections())
            .lastBackupDate(getLastBackupDate())
            .systemUptime(getSystemUptime())
            .build();
    }
    
    /**
     * Log admin action
     */
    private void logAdminAction(User adminUser, AdminActionType actionType, 
                              String entityType, Long entityId, String entityName,
                              String oldValues, String newValues) {
        AdminAction action = AdminAction.builder()
            .adminUser(adminUser)
            .actionType(actionType)
            .entityType(entityType)
            .entityId(entityId)
            .entityName(entityName)
            .actionDescription(generateActionDescription(actionType, entityType, entityName))
            .oldValues(oldValues)
            .newValues(newValues)
            .actionTimestamp(LocalDateTime.now())
            .status(ActionStatus.SUCCESS)
            .build();
        
        adminActionRepository.save(action);
    }
    
    private List<Player> generatePlayersForTeam(Team team, int count, QualityRange qualityRange) {
        List<Player> players = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Player player = playerGenerationService.generatePlayer(
                team.getClub().getCountry(),
                qualityRange.getMinOverall(),
                qualityRange.getMaxOverall()
            );
            
            player.setTeam(team);
            player = playerRepository.save(player);
            players.add(player);
        }
        
        return players;
    }
    
    private void validateConfigValue(SystemConfiguration config, String newValue) {
        switch (config.getValueType()) {
            case INTEGER -> {
                try {
                    Integer.parseInt(newValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Value must be an integer");
                }
            }
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(newValue) && !"false".equalsIgnoreCase(newValue)) {
                    throw new IllegalArgumentException("Value must be true or false");
                }
            }
            case DECIMAL -> {
                try {
                    new BigDecimal(newValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Value must be a valid decimal");
                }
            }
        }
        
        // Apply validation rules if present
        if (config.getValidationRules() != null) {
            applyValidationRules(config.getValidationRules(), newValue);
        }
    }
}
```

### API Endpoints

#### AdminController
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        AdminDashboardDTO dashboard = adminService.getAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }
    
    @PostMapping("/clubs")
    public ResponseEntity<ClubDTO> createClub(@RequestBody CreateClubRequest request,
                                            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        Club club = adminService.createClub(request, adminUser);
        return ResponseEntity.ok(convertToDTO(club));
    }
    
    @PutMapping("/clubs/{clubId}")
    public ResponseEntity<ClubDTO> updateClub(@PathVariable Long clubId,
                                            @RequestBody UpdateClubRequest request,
                                            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        Club club = adminService.updateClub(clubId, request, adminUser);
        return ResponseEntity.ok(convertToDTO(club));
    }
    
    @DeleteMapping("/clubs/{clubId}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long clubId,
                                         Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        adminService.deleteClub(clubId, adminUser);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/players/bulk-update")
    public ResponseEntity<BulkUpdateResult> bulkUpdatePlayers(
            @RequestBody BulkUpdatePlayersRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        BulkUpdateResult result = adminService.bulkUpdatePlayers(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/leagues/{leagueId}/generate-players")
    public ResponseEntity<PlayerGenerationResult> generatePlayersForLeague(
            @PathVariable Long leagueId,
            @RequestBody PlayerGenerationRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        PlayerGenerationResult result = adminService.generatePlayersForLeague(leagueId, request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/import")
    public ResponseEntity<ImportResult> importData(@RequestBody ImportDataRequest request,
                                                 Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        ImportResult result = adminService.importData(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/export")
    public ResponseEntity<ExportResult> exportData(@RequestBody ExportDataRequest request,
                                                 Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        ExportResult result = adminService.exportData(request, adminUser);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/config")
    public ResponseEntity<List<SystemConfigurationDTO>> getSystemConfiguration(
            @RequestParam(required = false) ConfigCategory category) {
        List<SystemConfigurationDTO> configs = adminService.getSystemConfiguration(category);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/config/{configId}")
    public ResponseEntity<SystemConfigurationDTO> updateConfiguration(
            @PathVariable Long configId,
            @RequestBody UpdateConfigRequest request,
            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        SystemConfiguration config = adminService.updateSystemConfiguration(
            configId, request.getNewValue(), adminUser);
        return ResponseEntity.ok(convertToDTO(config));
    }
    
    @GetMapping("/actions")
    public ResponseEntity<Page<AdminActionDTO>> getAdminActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AdminActionType actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String adminUsername) {
        
        AdminActionFilter filter = AdminActionFilter.builder()
            .actionType(actionType)
            .entityType(entityType)
            .adminUsername(adminUsername)
            .build();
        
        Page<AdminActionDTO> actions = adminService.getAdminActionHistory(
            filter, PageRequest.of(page, size, Sort.by("actionTimestamp").descending()));
        
        return ResponseEntity.ok(actions);
    }
}
```

### Frontend Implementation (fm-admin)

#### AdminDashboard Component
```jsx
import React, { useState, useEffect } from 'react';
import { getAdminDashboard, getAdminActions } from '../services/api';
import { Line, Bar, Doughnut } from 'react-chartjs-2';

const AdminDashboard = () => {
    const [dashboard, setDashboard] = useState(null);
    const [recentActions, setRecentActions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            const [dashboardResponse, actionsResponse] = await Promise.all([
                getAdminDashboard(),
                getAdminActions(0, 10)
            ]);
            
            setDashboard(dashboardResponse.data);
            setRecentActions(actionsResponse.data.content);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Loading admin dashboard...</div>;

    return (
        <div className="admin-dashboard">
            <div className="dashboard-header">
                <h1>Admin Dashboard</h1>
                <div className="dashboard-actions">
                    <button className="btn-primary">Create Club</button>
                    <button className="btn-secondary">Import Data</button>
                    <button className="btn-secondary">System Settings</button>
                </div>
            </div>

            {/* Statistics Cards */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon clubs">
                        <i className="fas fa-shield-alt"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Total Clubs</h3>
                        <span className="stat-number">{dashboard.totalClubs}</span>
                    </div>
                </div>
                
                <div className="stat-card">
                    <div className="stat-icon players">
                        <i className="fas fa-users"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Total Players</h3>
                        <span className="stat-number">{dashboard.totalPlayers}</span>
                    </div>
                </div>
                
                <div className="stat-card">
                    <div className="stat-icon users">
                        <i className="fas fa-user"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Active Users</h3>
                        <span className="stat-number">{dashboard.activeUsers}</span>
                        <span className="stat-subtitle">of {dashboard.totalUsers} total</span>
                    </div>
                </div>
                
                <div className="stat-card">
                    <div className="stat-icon leagues">
                        <i className="fas fa-trophy"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Leagues</h3>
                        <span className="stat-number">{dashboard.totalLeagues}</span>
                    </div>
                </div>
            </div>

            {/* System Health */}
            <div className="system-health">
                <h2>System Health</h2>
                <div className="health-indicators">
                    <div className="health-item">
                        <span className="health-label">Database</span>
                        <span className={`health-status ${dashboard.systemHealth.databaseStatus.toLowerCase()}`}>
                            {dashboard.systemHealth.databaseStatus}
                        </span>
                    </div>
                    <div className="health-item">
                        <span className="health-label">Memory Usage</span>
                        <div className="memory-bar">
                            <div 
                                className="memory-fill"
                                style={{ width: `${dashboard.systemHealth.memoryUsage}%` }}
                            ></div>
                        </div>
                        <span className="health-value">{dashboard.systemHealth.memoryUsage}%</span>
                    </div>
                    <div className="health-item">
                        <span className="health-label">Active Connections</span>
                        <span className="health-value">{dashboard.systemHealth.activeConnections}</span>
                    </div>
                    <div className="health-item">
                        <span className="health-label">System Uptime</span>
                        <span className="health-value">{dashboard.systemHealth.systemUptime}</span>
                    </div>
                </div>
            </div>

            {/* Recent Admin Actions */}
            <div className="recent-actions">
                <h2>Recent Admin Actions</h2>
                <div className="actions-list">
                    {recentActions.map(action => (
                        <div key={action.id} className="action-item">
                            <div className="action-icon">
                                <i className={getActionIcon(action.actionType)}></i>
                            </div>
                            <div className="action-content">
                                <div className="action-description">
                                    {action.actionDescription}
                                </div>
                                <div className="action-meta">
                                    <span className="action-user">{action.adminUser.username}</span>
                                    <span className="action-time">
                                        {new Date(action.actionTimestamp).toLocaleString()}
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

            {/* Quick Actions */}
            <div className="quick-actions">
                <h2>Quick Actions</h2>
                <div className="actions-grid">
                    <div className="action-card" onClick={() => navigateToClubManagement()}>
                        <i className="fas fa-shield-alt"></i>
                        <h3>Manage Clubs</h3>
                        <p>Create, edit, and delete clubs</p>
                    </div>
                    
                    <div className="action-card" onClick={() => navigateToPlayerManagement()}>
                        <i className="fas fa-users"></i>
                        <h3>Manage Players</h3>
                        <p>Bulk operations on players</p>
                    </div>
                    
                    <div className="action-card" onClick={() => navigateToLeagueManagement()}>
                        <i className="fas fa-trophy"></i>
                        <h3>Manage Leagues</h3>
                        <p>Configure leagues and seasons</p>
                    </div>
                    
                    <div className="action-card" onClick={() => navigateToUserManagement()}>
                        <i className="fas fa-user-cog"></i>
                        <h3>User Management</h3>
                        <p>Manage user accounts and permissions</p>
                    </div>
                    
                    <div className="action-card" onClick={() => navigateToSystemConfig()}>
                        <i className="fas fa-cogs"></i>
                        <h3>System Configuration</h3>
                        <p>Adjust system settings</p>
                    </div>
                    
                    <div className="action-card" onClick={() => navigateToDataTools()}>
                        <i className="fas fa-database"></i>
                        <h3>Data Tools</h3>
                        <p>Import/export data</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

const getActionIcon = (actionType) => {
    switch (actionType) {
        case 'CREATE': return 'fas fa-plus';
        case 'UPDATE': return 'fas fa-edit';
        case 'DELETE': return 'fas fa-trash';
        case 'BULK_UPDATE': return 'fas fa-tasks';
        case 'IMPORT': return 'fas fa-upload';
        case 'EXPORT': return 'fas fa-download';
        default: return 'fas fa-cog';
    }
};

export default AdminDashboard;
```

#### ClubManagement Component
```jsx
import React, { useState, useEffect } from 'react';
import { getClubs, createClub, updateClub, deleteClub } from '../services/api';

const ClubManagement = () => {
    const [clubs, setClubs] = useState([]);
    const [selectedClub, setSelectedClub] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterLeague, setFilterLeague] = useState('');

    useEffect(() => {
        loadClubs();
    }, []);

    const loadClubs = async () => {
        try {
            const response = await getClubs();
            setClubs(response.data);
        } catch (error) {
            console.error('Error loading clubs:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateClub = async (clubData) => {
        try {
            await createClub(clubData);
            loadClubs(); // Refresh list
            setShowCreateModal(false);
        } catch (error) {
            console.error('Error creating club:', error);
        }
    };

    const handleUpdateClub = async (clubId, clubData) => {
        try {
            await updateClub(clubId, clubData);
            loadClubs(); // Refresh list
            setSelectedClub(null);
        } catch (error) {
            console.error('Error updating club:', error);
        }
    };

    const handleDeleteClub = async (clubId) => {
        if (window.confirm('Are you sure you want to delete this club? This action cannot be undone.')) {
            try {
                await deleteClub(clubId);
                loadClubs(); // Refresh list
            } catch (error) {
                console.error('Error deleting club:', error);
            }
        }
    };

    const filteredClubs = clubs.filter(club => {
        const matchesSearch = club.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                            club.shortName.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesLeague = !filterLeague || club.league.name === filterLeague;
        return matchesSearch && matchesLeague;
    });

    if (loading) return <div className="loading">Loading clubs...</div>;

    return (
        <div className="club-management">
            <div className="management-header">
                <h1>Club Management</h1>
                <button 
                    className="btn-primary"
                    onClick={() => setShowCreateModal(true)}
                >
                    Create New Club
                </button>
            </div>

            <div className="management-filters">
                <div className="search-box">
                    <input
                        type="text"
                        placeholder="Search clubs..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filter-box">
                    <select
                        value={filterLeague}
                        onChange={(e) => setFilterLeague(e.target.value)}
                    >
                        <option value="">All Leagues</option>
                        {/* Populate with available leagues */}
                    </select>
                </div>
            </div>

            <div className="clubs-table">
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Short Name</th>
                            <th>League</th>
                            <th>Founded</th>
                            <th>City</th>
                            <th>Stadium</th>
                            <th>Players</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredClubs.map(club => (
                            <tr key={club.id}>
                                <td>{club.name}</td>
                                <td>{club.shortName}</td>
                                <td>{club.league.name}</td>
                                <td>{club.foundedYear}</td>
                                <td>{club.city}</td>
                                <td>{club.stadium?.name || 'No Stadium'}</td>
                                <td>{club.team?.players?.length || 0}</td>
                                <td>
                                    <div className="action-buttons">
                                        <button 
                                            className="btn-secondary btn-sm"
                                            onClick={() => setSelectedClub(club)}
                                        >
                                            Edit
                                        </button>
                                        <button 
                                            className="btn-danger btn-sm"
                                            onClick={() => handleDeleteClub(club.id)}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Create Club Modal */}
            {showCreateModal && (
                <CreateClubModal
                    onClose={() => setShowCreateModal(false)}
                    onSubmit={handleCreateClub}
                />
            )}

            {/* Edit Club Modal */}
            {selectedClub && (
                <EditClubModal
                    club={selectedClub}
                    onClose={() => setSelectedClub(null)}
                    onSubmit={(data) => handleUpdateClub(selectedClub.id, data)}
                />
            )}
        </div>
    );
};

const CreateClubModal = ({ onClose, onSubmit }) => {
    const [formData, setFormData] = useState({
        name: '',
        shortName: '',
        foundedYear: new Date().getFullYear(),
        city: '',
        countryId: '',
        leagueId: '',
        initialBudget: 1000000,
        generateInitialSquad: true,
        squadQuality: 'AVERAGE',
        stadiumRequest: {
            name: '',
            capacity: 20000,
            pitchQuality: 5,
            facilitiesQuality: 5
        }
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content large">
                <div className="modal-header">
                    <h2>Create New Club</h2>
                    <button onClick={onClose}>×</button>
                </div>
                
                <form onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <div className="form-group">
                            <label>Club Name</label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                required
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>Short Name</label>
                            <input
                                type="text"
                                value={formData.shortName}
                                onChange={(e) => setFormData({...formData, shortName: e.target.value})}
                                maxLength="3"
                                required
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>Founded Year</label>
                            <input
                                type="number"
                                value={formData.foundedYear}
                                onChange={(e) => setFormData({...formData, foundedYear: parseInt(e.target.value)})}
                                min="1800"
                                max={new Date().getFullYear()}
                                required
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>City</label>
                            <input
                                type="text"
                                value={formData.city}
                                onChange={(e) => setFormData({...formData, city: e.target.value})}
                                required
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>Initial Budget</label>
                            <input
                                type="number"
                                value={formData.initialBudget}
                                onChange={(e) => setFormData({...formData, initialBudget: parseInt(e.target.value)})}
                                min="0"
                                step="100000"
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>
                                <input
                                    type="checkbox"
                                    checked={formData.generateInitialSquad}
                                    onChange={(e) => setFormData({...formData, generateInitialSquad: e.target.checked})}
                                />
                                Generate Initial Squad
                            </label>
                        </div>
                    </div>
                    
                    <div className="stadium-section">
                        <h3>Stadium Details</h3>
                        <div className="form-grid">
                            <div className="form-group">
                                <label>Stadium Name</label>
                                <input
                                    type="text"
                                    value={formData.stadiumRequest.name}
                                    onChange={(e) => setFormData({
                                        ...formData,
                                        stadiumRequest: {...formData.stadiumRequest, name: e.target.value}
                                    })}
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Capacity</label>
                                <input
                                    type="number"
                                    value={formData.stadiumRequest.capacity}
                                    onChange={(e) => setFormData({
                                        ...formData,
                                        stadiumRequest: {...formData.stadiumRequest, capacity: parseInt(e.target.value)}
                                    })}
                                    min="5000"
                                    max="100000"
                                />
                            </div>
                        </div>
                    </div>
                    
                    <div className="modal-actions">
                        <button type="button" onClick={onClose} className="btn-secondary">
                            Cancel
                        </button>
                        <button type="submit" className="btn-primary">
                            Create Club
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ClubManagement;
```

## Implementation Notes

1. **Role-Based Access**: Different admin roles with varying permissions
2. **Audit Trail**: Complete logging of all administrative actions
3. **Bulk Operations**: Efficient bulk updates for large datasets
4. **Data Import/Export**: Support for CSV and JSON formats
5. **System Configuration**: Runtime configuration changes without restart
6. **Validation**: Comprehensive validation for all admin operations

## Dependencies

- User authentication and authorization system
- All core entities (Club, Player, League, etc.)
- File upload/download capabilities
- Email service for notifications
- Logging framework for audit trails

## Testing Strategy

1. **Unit Tests**: Test all admin service methods
2. **Integration Tests**: Test complete admin workflows
3. **Security Tests**: Verify role-based access controls
4. **Performance Tests**: Test bulk operations with large datasets
5. **User Acceptance Tests**: Test admin interface usability