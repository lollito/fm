package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.mapper.ClubMapper;
import com.lollito.fm.mapper.SystemConfigurationMapper;
import com.lollito.fm.model.AdminActionType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.ConfigCategory;
import com.lollito.fm.model.SystemConfiguration;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.AdminActionDTO;
import com.lollito.fm.model.dto.AdminActionFilter;
import com.lollito.fm.model.dto.AdminDashboardDTO;
import com.lollito.fm.model.dto.BulkUpdatePlayersRequest;
import com.lollito.fm.model.dto.BulkUpdateResult;
import com.lollito.fm.model.dto.ClubDTO;
import com.lollito.fm.model.dto.CreateClubRequest;
import com.lollito.fm.model.dto.ExportDataRequest;
import com.lollito.fm.model.dto.ExportResult;
import com.lollito.fm.model.dto.ImportDataRequest;
import com.lollito.fm.model.dto.ImportResult;
import com.lollito.fm.model.dto.PlayerGenerationRequest;
import com.lollito.fm.model.dto.PlayerGenerationResult;
import com.lollito.fm.model.dto.SystemConfigurationDTO;
import com.lollito.fm.model.dto.UpdateClubRequest;
import com.lollito.fm.model.dto.UpdateConfigRequest;
import com.lollito.fm.service.AdminService;
import com.lollito.fm.service.UserService;

import jakarta.validation.Valid;

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
    public ResponseEntity<ClubDTO> createClub(@Valid @RequestBody CreateClubRequest request,
                                            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        Club club = adminService.createClub(request, adminUser);
        return ResponseEntity.ok(clubMapper.toDto(club));
    }

    @PutMapping("/clubs/{clubId}")
    public ResponseEntity<ClubDTO> updateClub(@PathVariable Long clubId,
                                            @Valid @RequestBody UpdateClubRequest request,
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
