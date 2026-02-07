package com.lollito.fm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.BanUserRequest;
import com.lollito.fm.model.dto.CreateUserRequest;
import com.lollito.fm.model.dto.UpdateUserRequest;
import com.lollito.fm.model.dto.UserActivityDTO;
import com.lollito.fm.model.dto.UserDTO;
import com.lollito.fm.model.dto.UserFilter;
import com.lollito.fm.model.dto.UserManagementDashboardDTO;
import com.lollito.fm.model.dto.UserSessionDTO;
import com.lollito.fm.service.UserManagementService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<UserManagementDashboardDTO> getDashboard() {
        UserManagementDashboardDTO dashboard = userManagementService.getUserManagementDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean isBanned,
            @RequestParam(required = false) String role) {

        UserFilter filter = UserFilter.builder()
            .search(search)
            .isActive(isActive)
            .isVerified(isVerified)
            .isBanned(isBanned)
            .role(role)
            .build();

        Page<UserDTO> users = userManagementService.getUsers(
            filter, PageRequest.of(page, size, Sort.by("createdDate").descending()));

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request,
                                            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        User user = userManagementService.createUser(request, adminUser);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId,
                                            @RequestBody UpdateUserRequest request,
                                            Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        User user = userManagementService.updateUser(userId, request, adminUser);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PostMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long userId,
                                      @RequestBody BanUserRequest request,
                                      Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        userManagementService.banUser(userId, request, adminUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId,
                                        Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        userManagementService.unbanUser(userId, adminUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long userId,
                                                 Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        userManagementService.resetUserPassword(userId, adminUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/activities")
    public ResponseEntity<Page<UserActivityDTO>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<UserActivityDTO> activities = userManagementService.getUserActivityHistory(
            userId, PageRequest.of(page, size));

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{userId}/sessions")
    public ResponseEntity<List<UserSessionDTO>> getUserSessions(@PathVariable Long userId) {
        List<UserSessionDTO> sessions = userManagementService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/sessions/{sessionId}/terminate")
    public ResponseEntity<Void> terminateSession(@PathVariable Long sessionId,
                                                Authentication authentication) {
        User adminUser = getAdminUser(authentication);
        userManagementService.terminateUserSession(sessionId, adminUser);
        return ResponseEntity.ok().build();
    }

    private User getAdminUser(Authentication authentication) {
        return userService.getUser(authentication.getName());
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .country(user.getCountryString())
            .dateOfBirth(user.getDateOfBirth())
            .preferredLanguage(user.getPreferredLanguage())
            .timezone(user.getTimezone())
            .isActive(user.getIsActive())
            .isVerified(user.getIsVerified())
            .isBanned(user.getIsBanned())
            .banReason(user.getBanReason())
            .bannedUntil(user.getBannedUntil())
            .createdDate(user.getCreatedDate())
            .lastLoginDate(user.getLastLoginDate())
            .roles(user.getRoles())
            .build();
    }
}
