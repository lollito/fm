package com.lollito.fm.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.model.ActionStatus;
import com.lollito.fm.model.AdminAction;
import com.lollito.fm.model.AdminActionType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.ConfigCategory;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.model.SystemConfiguration;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.AdminActionDTO;
import com.lollito.fm.model.dto.AdminActionFilter;
import com.lollito.fm.model.dto.AdminDashboardDTO;
import com.lollito.fm.model.dto.BulkUpdatePlayersRequest;
import com.lollito.fm.model.dto.BulkUpdateResult;
import com.lollito.fm.model.dto.CreateClubRequest;
import com.lollito.fm.model.dto.ExportDataRequest;
import com.lollito.fm.model.dto.ExportResult;
import com.lollito.fm.model.dto.ImportDataRequest;
import com.lollito.fm.model.dto.ImportResult;
import com.lollito.fm.model.dto.PlayerGenerationRequest;
import com.lollito.fm.model.dto.PlayerGenerationResult;
import com.lollito.fm.model.dto.QualityRange;
import com.lollito.fm.model.dto.SystemConfigurationDTO;
import com.lollito.fm.model.dto.SystemHealthDTO;
import com.lollito.fm.model.dto.UpdateClubRequest;
import com.lollito.fm.repository.rest.AdminActionRepository;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.LeagueRepository;
import com.lollito.fm.repository.rest.PlayerRepository;
import com.lollito.fm.repository.rest.SystemConfigurationRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.utils.RandomUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;

@Service
public class AdminService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private ClubRepository clubRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private LeagueRepository leagueRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AdminActionRepository adminActionRepository;
    @Autowired private SystemConfigurationRepository systemConfigRepository;
    @Autowired private CountryService countryService;
    @Autowired private LeagueService leagueService;
    @Autowired private ClubService clubService;
    @Autowired private TeamService teamService;
    @Autowired private PlayerService playerService;
    @Autowired private NameService nameService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private List<AdminActionDTO> getRecentAdminActions(int limit) {
         // This is a simplified fetch, ideally use PageRequest
         // But since we need to return List, we can use findAll with pageable
         return adminActionRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("actionTimestamp").descending()))
                 .getContent()
                 .stream()
                 .map(this::convertToDTO)
                 .collect(Collectors.toList());
    }

    /**
     * Create new club with complete setup
     */
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Transactional
    public Club createClub(CreateClubRequest request, User adminUser) {
        // Create club
        Club club = new Club();
        club.setName(request.getName());
        club.setShortName(request.getShortName());
        club.setFoundation(java.time.LocalDate.of(request.getFoundedYear(), 1, 1));
        club.setCity(request.getCity());

        if (request.getLeagueId() != null) {
             club.setLeague(leagueRepository.findById(request.getLeagueId())
                     .orElseThrow(() -> new EntityNotFoundException("League not found")));
        }

        club = clubRepository.save(club);

        // Create stadium
        if (request.getStadiumRequest() != null) {
            Stadium stadium = new Stadium();
            stadium.setName(request.getStadiumRequest().getName());

            if (request.getStadiumRequest().getCapacity() != null) {
                int cap = request.getStadiumRequest().getCapacity();
                int quarter = cap / 4;
                stadium.setGrandstandNord(quarter);
                stadium.setGrandstandSud(quarter);
                stadium.setGrandstandWest(quarter);
                stadium.setGrandstandEst(cap - (quarter * 3));
            }
            club.setStadium(stadium);
        } else {
             club.setStadium(new Stadium(club.getName() + " Stadium"));
        }

        // Create team
        Team team = new Team();
        club.setTeam(team);

        // Create finance
        Finance finance = new Finance(new BigDecimal(request.getInitialBudget() != null ? request.getInitialBudget() : 1000000));
        club.setFinance(finance);

        // Generate initial players if requested
        if (Boolean.TRUE.equals(request.getGenerateInitialSquad())) {
            // Use TeamService or generate manually
             Team generatedTeam = teamService.createTeam();
             team.setPlayers(generatedTeam.getPlayers());
             // need to re-associate players with this team?
             for(Player p : team.getPlayers()) {
                 p.setTeam(team);
             }
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
    public Club updateClub(Long clubId, UpdateClubRequest request, User adminUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found"));

        String oldValues = convertToJson(club); // Simplified, ideally deep copy or select fields

        if (request.getName() != null) club.setName(request.getName());
        if (request.getShortName() != null) club.setShortName(request.getShortName());
        if (request.getFoundedYear() != null) club.setFoundation(java.time.LocalDate.of(request.getFoundedYear(), 1, 1));
        if (request.getCity() != null) club.setCity(request.getCity());

        // Update stadium if provided
        if (request.getStadiumRequest() != null) {
            Stadium stadium = club.getStadium();
            if (stadium == null) {
                stadium = new Stadium();
                club.setStadium(stadium);
            }
            if (request.getStadiumRequest().getName() != null) stadium.setName(request.getStadiumRequest().getName());
            if (request.getStadiumRequest().getCapacity() != null) {
                int cap = request.getStadiumRequest().getCapacity();
                int quarter = cap / 4;
                stadium.setGrandstandNord(quarter);
                stadium.setGrandstandSud(quarter);
                stadium.setGrandstandWest(quarter);
                stadium.setGrandstandEst(cap - (quarter * 3));
            }
        }

        club = clubRepository.save(club);

        logAdminAction(adminUser, AdminActionType.UPDATE, "Club", club.getId(),
                      club.getName(), oldValues, convertToJson(club));

        return club;
    }

    @Transactional
    public void deleteClub(Long clubId, User adminUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found"));

        String oldValues = convertToJson(club);

        clubRepository.delete(club);

        logAdminAction(adminUser, AdminActionType.DELETE, "Club", clubId,
                      club.getName(), oldValues, null);
    }

    @Transactional
    public BulkUpdateResult bulkUpdatePlayers(BulkUpdatePlayersRequest request, User adminUser) {
        List<Player> players = playerRepository.findAllById(request.getPlayerIds());
        List<Player> updatedPlayers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Player player : players) {
            try {
                // Apply updates based on request
                if (request.getSalaryMultiplier() != null) {
                    BigDecimal newSalary = player.getSalary().multiply(request.getSalaryMultiplier());
                    player.setSalary(newSalary);
                }

                // Skill adjustment - simplified
                if (request.getSkillAdjustment() != null) {
                    player.updateSkills(request.getSkillAdjustment());
                }

                if (request.getNewClubId() != null) {
                    Club newClub = clubService.findById(request.getNewClubId());
                    // transfer logic
                    player.setTeam(newClub.getTeam());
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
            .updatedEntities(new ArrayList<>(updatedPlayers))
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
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new EntityNotFoundException("League not found"));
        List<Club> clubs = league.getClubs();
        List<Player> generatedPlayers = new ArrayList<>();

        for (Club club : clubs) {
            Team team = club.getTeam();
            if(team == null) continue;

            int currentSquadSize = team.getPlayers().size();
            int playersNeeded = Math.max(0, request.getTargetSquadSize() - currentSquadSize);

            if (playersNeeded > 0) {
                List<Player> newPlayers = generatePlayersForTeam(team, playersNeeded, request.getQualityRange());
                generatedPlayers.addAll(newPlayers);
            }
        }

        // Log admin action
        logAdminAction(adminUser, AdminActionType.CREATE, "Player", null,
                      "Generated " + generatedPlayers.size() + " players for league " + league.getName(),
                      null, convertToJson(request));

        return PlayerGenerationResult.builder()
            .totalGenerated(generatedPlayers.size())
            .playersPerClub(clubs.isEmpty() ? 0 : generatedPlayers.size() / clubs.size())
            .generatedPlayers(new ArrayList<>(generatedPlayers))
            .build();
    }

    /**
     * Import data from CSV/JSON - Placeholder
     */
    @Transactional
    public ImportResult importData(ImportDataRequest request, User adminUser) {
        // Implementation omitted for brevity/scope, returning failed result
        ImportResult result = ImportResult.builder()
            .totalRecords(0)
            .successfulImports(0)
            .failedImports(0)
            .errors(new ArrayList<>())
            .build();

        result.getErrors().add("Import not implemented yet");

        logAdminAction(adminUser, AdminActionType.IMPORT, request.getDataType().toString(), null,
                      "Import attempted (Not Implemented)",
                      null, convertToJson(request));

        return result;
    }

    /**
     * Export data - Placeholder
     */
    public ExportResult exportData(ExportDataRequest request, User adminUser) {
         ExportResult result = ExportResult.builder()
                .success(false)
                .errorMessage("Export not implemented yet")
                .build();

         logAdminAction(adminUser, AdminActionType.EXPORT, request.getDataType().toString(), null,
                      "Export attempted (Not Implemented)",
                      null, convertToJson(request));
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

        if (Boolean.FALSE.equals(config.getIsEditable())) {
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
     * Get system health status
     */
    private SystemHealthDTO getSystemHealthStatus() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsage = (double) usedMemory / maxMemory * 100;

        return SystemHealthDTO.builder()
            .databaseStatus("ONLINE") // Simplified
            .memoryUsage(Math.round(memoryUsage * 100.0) / 100.0)
            .activeConnections(0) // Need DB metric
            .lastBackupDate("N/A")
            .systemUptime(formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()))
            .build();
    }

    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
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

    private String generateActionDescription(AdminActionType actionType, String entityType, String entityName) {
        return actionType.getDisplayName() + " " + entityType + (entityName != null ? " - " + entityName : "");
    }

    private List<Player> generatePlayersForTeam(Team team, int count, QualityRange qualityRange) {
        List<Player> players = new ArrayList<>();
        List<String> names = nameService.getNames();
        List<String> surnames = nameService.getSurnames();

        for (int i = 0; i < count; i++) {
            // Randomly pick a role
            int roleVal = RandomUtils.randomValue(0, 5);

            Player player = new Player();
            player.setName(RandomUtils.randomValueFromList(names));
            player.setSurname(RandomUtils.randomValueFromList(surnames));
            player.setBirth(java.time.LocalDate.now().minusYears(RandomUtils.randomValue(16, 35)));

            // Use PlayerService to set stats based on role
            switch(roleVal) {
                case 0: playerService.createGk(player); break;
                case 1: playerService.createCd(player); break;
                case 2: playerService.createWb(player); break;
                case 3: playerService.createMf(player); break;
                case 4: playerService.createWng(player); break;
                case 5: playerService.createFw(player); break;
                default: playerService.createMf(player);
            }

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
            default -> {}
        }
    }

    private Specification<AdminAction> buildAdminActionSpecification(AdminActionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getActionType() != null) {
                predicates.add(cb.equal(root.get("actionType"), filter.getActionType()));
            }

            if (filter.getEntityType() != null && !filter.getEntityType().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("entityType")), "%" + filter.getEntityType().toLowerCase() + "%"));
            }

            if (filter.getAdminUsername() != null && !filter.getAdminUsername().isEmpty()) {
                 // Join with user
                 // predicates.add(cb.equal(root.get("adminUser").get("username"), filter.getAdminUsername()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Error converting object to JSON", e);
            return null;
        }
    }

    private AdminActionDTO convertToDTO(AdminAction action) {
        return AdminActionDTO.builder()
            .id(action.getId())
            .adminUsername(action.getAdminUser() != null ? action.getAdminUser().getUsername() : null)
            .actionType(action.getActionType())
            .entityType(action.getEntityType())
            .entityId(action.getEntityId())
            .entityName(action.getEntityName())
            .actionDescription(action.getActionDescription())
            .actionTimestamp(action.getActionTimestamp())
            .status(action.getStatus())
            .build();
    }

    private SystemConfigurationDTO convertToDTO(SystemConfiguration config) {
        return SystemConfigurationDTO.builder()
            .id(config.getId())
            .configKey(config.getConfigKey())
            .configValue(config.getConfigValue())
            .configDescription(config.getConfigDescription())
            .category(config.getCategory())
            .valueType(config.getValueType())
            .isEditable(config.getIsEditable())
            .lastModified(config.getLastModified())
            .modifiedBy(config.getModifiedBy())
            .build();
    }
}
