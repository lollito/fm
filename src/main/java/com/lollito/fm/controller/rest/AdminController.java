package com.lollito.fm.controller.rest;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.lollito.fm.model.*;
import com.lollito.fm.model.dto.*;
import com.lollito.fm.service.AdminService;
import com.lollito.fm.service.UserService;
import com.lollito.fm.mapper.ClubMapper;
import com.lollito.fm.mapper.SystemConfigurationMapper;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClubMapper clubMapper;
    @Autowired
    private SystemConfigurationMapper systemConfigurationMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        AdminDashboardDTO dashboard = adminService.getAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/clubs")
    public ResponseEntity<List<ClubDTO>> getClubs() {
        return ResponseEntity.ok(adminService.getAllClubs().stream()
                .map(clubMapper::toDto)
                .collect(Collectors.toList()));
    }

    @PostMapping("/clubs")
    public ResponseEntity<ClubDTO> createClub(@RequestBody CreateClubRequest request,
                                            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        Club club = adminService.createClub(request, adminUser);
        return ResponseEntity.ok(clubMapper.toDto(club));
    }

    @PutMapping("/clubs/{clubId}")
    public ResponseEntity<ClubDTO> updateClub(@PathVariable Long clubId,
                                            @RequestBody UpdateClubRequest request,
                                            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        Club club = adminService.updateClub(clubId, request, adminUser);
        return ResponseEntity.ok(clubMapper.toDto(club));
    }

    @DeleteMapping("/clubs/{clubId}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long clubId,
                                         Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        adminService.deleteClub(clubId, adminUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/players/bulk-update")
    public ResponseEntity<BulkUpdateResult> bulkUpdatePlayers(
            @RequestBody BulkUpdatePlayersRequest request,
            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        BulkUpdateResult result = adminService.bulkUpdatePlayers(request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/leagues/{leagueId}/generate-players")
    public ResponseEntity<PlayerGenerationResult> generatePlayersForLeague(
            @PathVariable Long leagueId,
            @RequestBody PlayerGenerationRequest request,
            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        PlayerGenerationResult result = adminService.generatePlayersForLeague(leagueId, request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResult> importData(@RequestBody ImportDataRequest request,
                                                 Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        ImportResult result = adminService.importData(request, adminUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/export")
    public ResponseEntity<ExportResult> exportData(@RequestBody ExportDataRequest request,
                                                 Authentication authentication) {
        User adminUser = getAdminUser(authentication);
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
        User adminUser = getAdminUser(authentication);
        SystemConfiguration config = adminService.updateSystemConfiguration(
            configId, request.getNewValue(), adminUser);
        return ResponseEntity.ok(systemConfigurationMapper.toDto(config));
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

    private User getAdminUser(Authentication authentication) {
        if(authentication != null && authentication.getName() != null) {
            return userService.getUser(authentication.getName());
        }
        // Fallback for non-authenticated testing env (if allowed) or throws
        // In production this should throw.
        throw new RuntimeException("User not authenticated");
    }
}
