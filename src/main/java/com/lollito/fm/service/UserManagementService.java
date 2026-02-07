package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.ActivitySeverity;
import com.lollito.fm.model.ActivityType;
import com.lollito.fm.model.NotificationPriority;
import com.lollito.fm.model.NotificationType;
import com.lollito.fm.model.PasswordResetRequest;
import com.lollito.fm.model.ResetRequestStatus;
import com.lollito.fm.model.Role;
import com.lollito.fm.model.SessionStatus;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserActivity;
import com.lollito.fm.model.UserNotification;
import com.lollito.fm.model.UserSession;
import com.lollito.fm.model.dto.BanUserRequest;
import com.lollito.fm.model.dto.CreateUserRequest;
import com.lollito.fm.model.dto.RoleDTO;
import com.lollito.fm.model.dto.UpdateUserRequest;
import com.lollito.fm.model.dto.UserActivityDTO;
import com.lollito.fm.model.dto.UserDTO;
import com.lollito.fm.model.dto.UserFilter;
import com.lollito.fm.model.dto.UserManagementDashboardDTO;
import com.lollito.fm.model.dto.UserSessionDTO;
import com.lollito.fm.repository.rest.PasswordResetRequestRepository;
import com.lollito.fm.repository.rest.RoleRepository;
import com.lollito.fm.repository.rest.UserActivityRepository;
import com.lollito.fm.repository.rest.UserNotificationRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.UserSessionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;

@Service
public class UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtils jwtUtils;

    public UserManagementDashboardDTO getUserManagementDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24Hours = now.minusHours(24);
        LocalDateTime last7Days = now.minusDays(7);

        return UserManagementDashboardDTO.builder()
            .totalUsers(userRepository.count())
            .activeUsers(userRepository.countByIsActive(true))
            .verifiedUsers(userRepository.countByIsVerified(true))
            .bannedUsers(userRepository.countByIsBanned(true))
            .newUsersLast24Hours(userRepository.countByCreatedDateAfter(last24Hours))
            .newUsersLast7Days(userRepository.countByCreatedDateAfter(last7Days))
            .activeSessionsCount(userSessionRepository.countByStatusAndIsActive(SessionStatus.ACTIVE, true))
            .recentActivities(getRecentUserActivities(20))
            .userRegistrationTrend(getUserRegistrationTrend())
            .build();
    }

    public Page<UserDTO> getUsers(UserFilter filter, Pageable pageable) {
        Specification<User> spec = buildUserSpecification(filter);
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::convertToDTO);
    }

    @Transactional
    public User createUser(CreateUserRequest request, User adminUser) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .dateOfBirth(request.getDateOfBirth())
            .countryString(request.getCountry())
            .preferredLanguage(request.getPreferredLanguage())
            .timezone(request.getTimezone())
            .password(passwordEncoder.encode(request.getPassword()))
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .isVerified(request.getIsVerified() != null ? request.getIsVerified() : false)
            .createdDate(LocalDateTime.now())
            .createdBy(adminUser.getUsername())
            .passwordChangedDate(LocalDateTime.now())
            .build();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            java.util.Set<Role> roles = request.getRoles().stream()
                    .map(roleDTO -> {
                        if (roleDTO.getId() != null) {
                            return roleRepository.findById(roleDTO.getId()).orElse(null);
                        }
                        return roleRepository.findByName(roleDTO.getName());
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        user = userRepository.save(user);

        if (Boolean.TRUE.equals(user.getIsActive()) && Boolean.TRUE.equals(user.getIsVerified())) {
            emailService.sendWelcomeEmail(user);
        } else if (Boolean.TRUE.equals(user.getIsActive()) && !Boolean.TRUE.equals(user.getIsVerified())) {
            sendEmailVerification(user);
        }

        logUserActivity(user, ActivityType.SYSTEM_ACTION,
                       "User account created by admin: " + adminUser.getUsername(),
                       ActivitySeverity.INFO);

        return user;
    }

    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
            user.setIsVerified(false);
            sendEmailVerification(user);
        }

        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getCountry() != null) user.setCountryString(request.getCountry());
        if (request.getPreferredLanguage() != null) user.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone());
        if (request.getRoles() != null) {
            java.util.Set<Role> roles = request.getRoles().stream()
                    .map(roleDTO -> {
                        if (roleDTO.getId() != null) {
                            return roleRepository.findById(roleDTO.getId()).orElse(null);
                        }
                        return roleRepository.findByName(roleDTO.getName());
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        user.setLastModifiedDate(LocalDateTime.now());
        user.setLastModifiedBy(adminUser.getUsername());

        user = userRepository.save(user);

        logUserActivity(user, ActivityType.PROFILE_UPDATE,
                       "User profile updated by admin: " + adminUser.getUsername(),
                       ActivitySeverity.INFO);

        return user;
    }

    @Transactional
    public void banUser(Long userId, BanUserRequest request, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setIsBanned(true);
        user.setBanReason(request.getReason());
        user.setBannedBy(adminUser.getUsername());

        if (request.getBanDuration() != null) {
            user.setBannedUntil(LocalDateTime.now().plus(request.getBanDuration()));
        }

        userRepository.save(user);
        terminateAllUserSessions(userId, "User banned");
        emailService.sendBanNotificationEmail(user, request.getReason());

        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Account Suspended",
                             "Your account has been suspended. Reason: " + request.getReason(),
                             NotificationPriority.URGENT);

        logUserActivity(user, ActivityType.SYSTEM_ACTION,
                       "User banned by admin: " + adminUser.getUsername() + ". Reason: " + request.getReason(),
                       ActivitySeverity.WARNING);
    }

    @Transactional
    public void unbanUser(Long userId, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setIsBanned(false);
        user.setBanReason(null);
        user.setBannedUntil(null);
        user.setBannedBy(null);

        userRepository.save(user);
        emailService.sendUnbanNotificationEmail(user);

        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Account Restored",
                             "Your account has been restored and is now active.",
                             NotificationPriority.HIGH);

        logUserActivity(user, ActivityType.SYSTEM_ACTION,
                       "User unbanned by admin: " + adminUser.getUsername(),
                       ActivitySeverity.INFO);
    }

    @Transactional
    public void resetUserPassword(Long userId, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setPasswordChangedDate(LocalDateTime.now());
        user.setLastModifiedDate(LocalDateTime.now());
        user.setLastModifiedBy(adminUser.getUsername());

        userRepository.save(user);
        emailService.sendTemporaryPasswordEmail(user, temporaryPassword);

        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Password Reset",
                             "Your password has been reset by an administrator. Check your email for the temporary password.",
                             NotificationPriority.HIGH);

        logUserActivity(user, ActivityType.PASSWORD_CHANGE,
                       "Password reset by admin: " + adminUser.getUsername(),
                       ActivitySeverity.WARNING);
    }

    @Transactional
    public void requestPasswordReset(String email, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive()) || Boolean.TRUE.equals(user.getIsBanned())) {
            throw new IllegalStateException("Cannot reset password for inactive or banned user");
        }

        String resetToken = generateSecureToken();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
            .user(user)
            .resetToken(resetToken)
            .tokenExpiry(tokenExpiry)
            .status(ResetRequestStatus.PENDING)
            .requestIpAddress(ipAddress)
            .requestUserAgent(userAgent)
            .requestedAt(LocalDateTime.now())
            .emailAddress(email)
            .build();

        resetRequest = passwordResetRequestRepository.save(resetRequest);

        boolean emailSent = emailService.sendPasswordResetEmail(user, resetToken);
        resetRequest.setEmailSent(emailSent);
        resetRequest.setEmailSentAt(LocalDateTime.now());
        passwordResetRequestRepository.save(resetRequest);

        logUserActivity(user, ActivityType.PASSWORD_CHANGE,
                       "Password reset requested from IP: " + ipAddress,
                       ActivitySeverity.INFO);
    }

    @Transactional
    public void completePasswordReset(String resetToken, String newPassword,
                                    String ipAddress, String userAgent) {
        PasswordResetRequest resetRequest = passwordResetRequestRepository
            .findByResetTokenAndStatus(resetToken, ResetRequestStatus.PENDING)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetRequest.getTokenExpiry().isBefore(LocalDateTime.now())) {
            resetRequest.setStatus(ResetRequestStatus.EXPIRED);
            passwordResetRequestRepository.save(resetRequest);
            throw new IllegalArgumentException("Reset token has expired");
        }

        User user = resetRequest.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedDate(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        userRepository.save(user);

        resetRequest.setStatus(ResetRequestStatus.USED);
        resetRequest.setUsedAt(LocalDateTime.now());
        resetRequest.setUsedIpAddress(ipAddress);
        passwordResetRequestRepository.save(resetRequest);

        emailService.sendPasswordResetConfirmationEmail(user);

        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Password Changed",
                             "Your password has been successfully changed.",
                             NotificationPriority.MEDIUM);

        logUserActivity(user, ActivityType.PASSWORD_CHANGE,
                       "Password reset completed from IP: " + ipAddress,
                       ActivitySeverity.INFO);
    }

    public Page<UserActivityDTO> getUserActivityHistory(Long userId, Pageable pageable) {
        Page<UserActivity> activities = userActivityRepository
            .findByUserIdOrderByActivityTimestampDesc(userId, pageable);
        return activities.map(this::convertToDTO);
    }

    public List<UserSessionDTO> getUserSessions(Long userId) {
        List<UserSession> sessions = userSessionRepository
            .findByUserIdOrderByLoginTimeDesc(userId);
        return sessions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void terminateUserSession(Long sessionId, User adminUser) {
        UserSession session = userSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        session.setStatus(SessionStatus.TERMINATED);
        session.setIsActive(false);
        session.setLogoutTime(LocalDateTime.now());
        session.setTerminationReason("Terminated by admin: " + adminUser.getUsername());

        userSessionRepository.save(session);

        logUserActivity(session.getUser(), ActivityType.LOGOUT,
                       "Session terminated by admin: " + adminUser.getUsername(),
                       ActivitySeverity.WARNING);
    }

    private Specification<User> buildUserSpecification(UserFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String search = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), search),
                    cb.like(cb.lower(root.get("email")), search),
                    cb.like(cb.lower(root.get("firstName")), search),
                    cb.like(cb.lower(root.get("lastName")), search)
                ));
            }

            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            if (filter.getIsVerified() != null) {
                predicates.add(cb.equal(root.get("isVerified"), filter.getIsVerified()));
            }

            if (filter.getIsBanned() != null) {
                predicates.add(cb.equal(root.get("isBanned"), filter.getIsBanned()));
            }

            if (filter.getRole() != null && !filter.getRole().isEmpty()) {
                predicates.add(cb.like(root.join("roles").get("name"), "%" + filter.getRole() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<UserActivityDTO> getRecentUserActivities(int limit) {
        return userActivityRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("activityTimestamp").descending()))
                .map(this::convertToDTO)
                .getContent();
    }

    private Map<String, Long> getUserRegistrationTrend() {
        return new HashMap<>();
    }

    private void sendEmailVerification(User user) {
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(48));
        userRepository.save(user);

        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), verificationToken);
    }

    private void terminateAllUserSessions(Long userId, String reason) {
        List<UserSession> sessions = userSessionRepository.findByUserIdOrderByLoginTimeDesc(userId);
        List<UserSession> sessionsToUpdate = new ArrayList<>();
        for (UserSession session : sessions) {
            if (Boolean.TRUE.equals(session.getIsActive())) {
                session.setIsActive(false);
                session.setStatus(SessionStatus.TERMINATED);
                session.setTerminationReason(reason);
                session.setLogoutTime(LocalDateTime.now());
                sessionsToUpdate.add(session);
            }
        }
        if (!sessionsToUpdate.isEmpty()) {
            userSessionRepository.saveAll(sessionsToUpdate);
        }
    }

    private void logUserActivity(User user, ActivityType activityType,
                               String description, ActivitySeverity severity) {
        UserActivity activity = UserActivity.builder()
            .user(user)
            .activityType(activityType)
            .activityDescription(description)
            .activityTimestamp(LocalDateTime.now())
            .severity(severity)
            .build();

        userActivityRepository.save(activity);
    }

    private void createUserNotification(User user, NotificationType type,
                                      String title, String message,
                                      NotificationPriority priority) {
        UserNotification notification = UserNotification.builder()
            .user(user)
            .notificationType(type)
            .title(title)
            .message(message)
            .priority(priority)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();

        userNotificationRepository.save(notification);
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               System.currentTimeMillis();
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
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
            .roles(user.getRoles().stream()
                    .map(role -> RoleDTO.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .build())
                    .collect(Collectors.toSet()))
            .build();
    }

    private UserActivityDTO convertToDTO(UserActivity activity) {
        return UserActivityDTO.builder()
            .id(activity.getId())
            .activityType(activity.getActivityType())
            .activityDescription(activity.getActivityDescription())
            .activityTimestamp(activity.getActivityTimestamp())
            .severity(activity.getSeverity())
            .ipAddress(activity.getIpAddress())
            .build();
    }

    private UserSessionDTO convertToDTO(UserSession session) {
        return UserSessionDTO.builder()
            .id(session.getId())
            .deviceInfo(session.getDeviceInfo())
            .ipAddress(session.getIpAddress())
            .location(session.getLocation())
            .loginTime(session.getLoginTime())
            .lastActivityTime(session.getLastActivityTime())
            .status(session.getStatus())
            .isActive(session.getIsActive())
            .build();
    }
}
