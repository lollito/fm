# User Management System Implementation

## Overview
Implement comprehensive user management functionality including user account administration, password reset capabilities, role management, and system logging. Provide tools for moderating users and monitoring system activity.

## Technical Requirements

### Database Schema Changes

#### Enhanced User Entity (Additional fields)
```java
// Add to existing User entity
@Column(name = "email")
private String email;

@Column(name = "first_name")
private String firstName;

@Column(name = "last_name")
private String lastName;

@Column(name = "phone_number")
private String phoneNumber;

@Column(name = "date_of_birth")
private LocalDate dateOfBirth;

@Column(name = "country")
private String country;

@Column(name = "preferred_language")
private String preferredLanguage;

@Column(name = "timezone")
private String timezone;

// Account status fields
@Column(name = "is_active")
private Boolean isActive = true;

@Column(name = "is_verified")
private Boolean isVerified = false;

@Column(name = "is_banned")
private Boolean isBanned = false;

@Column(name = "ban_reason")
private String banReason;

@Column(name = "banned_until")
private LocalDateTime bannedUntil;

@Column(name = "banned_by")
private String bannedBy;

// Login tracking
@Column(name = "last_login_date")
private LocalDateTime lastLoginDate;

@Column(name = "last_login_ip")
private String lastLoginIp;

@Column(name = "failed_login_attempts")
private Integer failedLoginAttempts = 0;

@Column(name = "account_locked_until")
private LocalDateTime accountLockedUntil;

// Account creation and modification
@Column(name = "created_date")
private LocalDateTime createdDate;

@Column(name = "created_by")
private String createdBy;

@Column(name = "last_modified_date")
private LocalDateTime lastModifiedDate;

@Column(name = "last_modified_by")
private String lastModifiedBy;

// Password management
@Column(name = "password_changed_date")
private LocalDateTime passwordChangedDate;

@Column(name = "password_reset_token")
private String passwordResetToken;

@Column(name = "password_reset_token_expiry")
private LocalDateTime passwordResetTokenExpiry;

@Column(name = "email_verification_token")
private String emailVerificationToken;

@Column(name = "email_verification_token_expiry")
private LocalDateTime emailVerificationTokenExpiry;

// Relationships
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<UserSession> sessions = new ArrayList<>();

@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<UserActivity> activities = new ArrayList<>();

@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<UserNotification> notifications = new ArrayList<>();
```

#### New Entity: UserSession
```java
@Entity
@Table(name = "user_session")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String location; // Derived from IP
    
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;
    private LocalDateTime logoutTime;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status; // ACTIVE, EXPIRED, TERMINATED
    
    private Boolean isActive;
    private String terminationReason;
}
```

#### New Entity: UserActivity
```java
@Entity
@Table(name = "user_activity")
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    
    private String activityDescription;
    private String entityType; // Club, Player, Match, etc.
    private Long entityId;
    private String entityName;
    
    private LocalDateTime activityTimestamp;
    private String ipAddress;
    private String userAgent;
    
    // Activity metadata (JSON)
    private String activityData;
    
    @Enumerated(EnumType.STRING)
    private ActivitySeverity severity; // INFO, WARNING, ERROR
    
    private String sessionId;
}
```

#### New Entity: UserNotification
```java
@Entity
@Table(name = "user_notification")
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    
    private String title;
    private String message;
    private String actionUrl;
    
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;
    
    private Boolean isRead = false;
    private LocalDateTime readAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // Email notification tracking
    private Boolean emailSent = false;
    private LocalDateTime emailSentAt;
    private String emailStatus;
}
```

#### New Entity: PasswordResetRequest
```java
@Entity
@Table(name = "password_reset_request")
public class PasswordResetRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private String resetToken;
    private LocalDateTime tokenExpiry;
    
    @Enumerated(EnumType.STRING)
    private ResetRequestStatus status; // PENDING, USED, EXPIRED, CANCELLED
    
    private String requestIpAddress;
    private String requestUserAgent;
    private LocalDateTime requestedAt;
    
    private LocalDateTime usedAt;
    private String usedIpAddress;
    
    private String emailAddress; // Email where reset link was sent
    private Boolean emailSent;
    private LocalDateTime emailSentAt;
}
```

#### Enums to Create
```java
public enum SessionStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    TERMINATED("Terminated");
    
    private final String displayName;
}

public enum ActivityType {
    LOGIN("Login"),
    LOGOUT("Logout"),
    PASSWORD_CHANGE("Password Change"),
    PROFILE_UPDATE("Profile Update"),
    CLUB_ACTION("Club Action"),
    PLAYER_ACTION("Player Action"),
    MATCH_ACTION("Match Action"),
    TRANSFER_ACTION("Transfer Action"),
    FINANCIAL_ACTION("Financial Action"),
    SYSTEM_ACTION("System Action");
    
    private final String displayName;
}

public enum ActivitySeverity {
    INFO("Information"),
    WARNING("Warning"),
    ERROR("Error");
    
    private final String displayName;
}

public enum NotificationType {
    SYSTEM("System Notification"),
    MATCH_RESULT("Match Result"),
    TRANSFER_UPDATE("Transfer Update"),
    FINANCIAL_ALERT("Financial Alert"),
    ACCOUNT_SECURITY("Account Security"),
    PROMOTIONAL("Promotional");
    
    private final String displayName;
}

public enum NotificationPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");
    
    private final String displayName;
}

public enum ResetRequestStatus {
    PENDING("Pending"),
    USED("Used"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");
    
    private final String displayName;
}
```

### Service Layer Implementation

#### UserManagementService
```java
@Service
public class UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
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
    
    /**
     * Get user management dashboard
     */
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
    
    /**
     * Get paginated user list with filters
     */
    public Page<UserDTO> getUsers(UserFilter filter, Pageable pageable) {
        Specification<User> spec = buildUserSpecification(filter);
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::convertToDTO);
    }
    
    /**
     * Create new user account
     */
    @Transactional
    public User createUser(CreateUserRequest request, User adminUser) {
        // Validate request
        validateUserCreationRequest(request);
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        
        // Create user
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .dateOfBirth(request.getDateOfBirth())
            .country(request.getCountry())
            .preferredLanguage(request.getPreferredLanguage())
            .timezone(request.getTimezone())
            .password(passwordEncoder.encode(request.getPassword()))
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .isVerified(request.getIsVerified() != null ? request.getIsVerified() : false)
            .createdDate(LocalDateTime.now())
            .createdBy(adminUser.getUsername())
            .passwordChangedDate(LocalDateTime.now())
            .build();
        
        // Set roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(request.getRoles());
        } else {
            user.setRoles(Set.of(Role.USER)); // Default role
        }
        
        user = userRepository.save(user);
        
        // Send welcome email if user is active and verified
        if (user.getIsActive() && user.getIsVerified()) {
            sendWelcomeEmail(user);
        } else if (user.getIsActive() && !user.getIsVerified()) {
            sendEmailVerification(user);
        }
        
        // Log activity
        logUserActivity(user, ActivityType.SYSTEM_ACTION, 
                       "User account created by admin: " + adminUser.getUsername(),
                       ActivitySeverity.INFO);
        
        return user;
    }
    
    /**
     * Update user account
     */
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Store old values for audit
        String oldValues = convertToJson(user);
        
        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if new email already exists
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new UserAlreadyExistsException("Email already exists");
            }
            user.setEmail(request.getEmail());
            user.setIsVerified(false); // Require re-verification
            sendEmailVerification(user);
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getPreferredLanguage() != null) {
            user.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }
        
        user.setLastModifiedDate(LocalDateTime.now());
        user.setLastModifiedBy(adminUser.getUsername());
        
        user = userRepository.save(user);
        
        // Log activity
        logUserActivity(user, ActivityType.PROFILE_UPDATE, 
                       "User profile updated by admin: " + adminUser.getUsername(),
                       ActivitySeverity.INFO);
        
        return user;
    }
    
    /**
     * Ban user account
     */
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
        
        // Terminate all active sessions
        terminateAllUserSessions(userId, "User banned");
        
        // Send ban notification email
        sendBanNotificationEmail(user, request.getReason());
        
        // Create system notification
        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Account Suspended",
                             "Your account has been suspended. Reason: " + request.getReason(),
                             NotificationPriority.URGENT);
        
        // Log activity
        logUserActivity(user, ActivityType.SYSTEM_ACTION, 
                       "User banned by admin: " + adminUser.getUsername() + ". Reason: " + request.getReason(),
                       ActivitySeverity.WARNING);
    }
    
    /**
     * Unban user account
     */
    @Transactional
    public void unbanUser(Long userId, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setIsBanned(false);
        user.setBanReason(null);
        user.setBannedUntil(null);
        user.setBannedBy(null);
        
        userRepository.save(user);
        
        // Send unban notification email
        sendUnbanNotificationEmail(user);
        
        // Create system notification
        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Account Restored",
                             "Your account has been restored and is now active.",
                             NotificationPriority.HIGH);
        
        // Log activity
        logUserActivity(user, ActivityType.SYSTEM_ACTION, 
                       "User unbanned by admin: " + adminUser.getUsername(),
                       ActivitySeverity.INFO);
    }
    
    /**
     * Reset user password (admin initiated)
     */
    @Transactional
    public void resetUserPassword(Long userId, User adminUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setPasswordChangedDate(LocalDateTime.now());
        user.setLastModifiedDate(LocalDateTime.now());
        user.setLastModifiedBy(adminUser.getUsername());
        
        userRepository.save(user);
        
        // Send temporary password email
        sendTemporaryPasswordEmail(user, temporaryPassword);
        
        // Create system notification
        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Password Reset",
                             "Your password has been reset by an administrator. Check your email for the temporary password.",
                             NotificationPriority.HIGH);
        
        // Log activity
        logUserActivity(user, ActivityType.PASSWORD_CHANGE, 
                       "Password reset by admin: " + adminUser.getUsername(),
                       ActivitySeverity.WARNING);
    }
    
    /**
     * Process password reset request
     */
    @Transactional
    public void requestPasswordReset(String email, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Check if user is active and not banned
        if (!user.getIsActive() || user.getIsBanned()) {
            throw new IllegalStateException("Cannot reset password for inactive or banned user");
        }
        
        // Generate reset token
        String resetToken = generateSecureToken();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24); // 24 hour expiry
        
        // Create reset request
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
        
        // Send reset email
        boolean emailSent = sendPasswordResetEmail(user, resetToken);
        resetRequest.setEmailSent(emailSent);
        resetRequest.setEmailSentAt(LocalDateTime.now());
        passwordResetRequestRepository.save(resetRequest);
        
        // Log activity
        logUserActivity(user, ActivityType.PASSWORD_CHANGE, 
                       "Password reset requested from IP: " + ipAddress,
                       ActivitySeverity.INFO);
    }
    
    /**
     * Complete password reset
     */
    @Transactional
    public void completePasswordReset(String resetToken, String newPassword, 
                                    String ipAddress, String userAgent) {
        PasswordResetRequest resetRequest = passwordResetRequestRepository
            .findByResetTokenAndStatus(resetToken, ResetRequestStatus.PENDING)
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));
        
        // Check token expiry
        if (resetRequest.getTokenExpiry().isBefore(LocalDateTime.now())) {
            resetRequest.setStatus(ResetRequestStatus.EXPIRED);
            passwordResetRequestRepository.save(resetRequest);
            throw new InvalidTokenException("Reset token has expired");
        }
        
        User user = resetRequest.getUser();
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedDate(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // Reset failed attempts
        user.setAccountLockedUntil(null); // Unlock account if locked
        
        userRepository.save(user);
        
        // Mark reset request as used
        resetRequest.setStatus(ResetRequestStatus.USED);
        resetRequest.setUsedAt(LocalDateTime.now());
        resetRequest.setUsedIpAddress(ipAddress);
        passwordResetRequestRepository.save(resetRequest);
        
        // Send confirmation email
        sendPasswordResetConfirmationEmail(user);
        
        // Create system notification
        createUserNotification(user, NotificationType.ACCOUNT_SECURITY,
                             "Password Changed",
                             "Your password has been successfully changed.",
                             NotificationPriority.MEDIUM);
        
        // Log activity
        logUserActivity(user, ActivityType.PASSWORD_CHANGE, 
                       "Password reset completed from IP: " + ipAddress,
                       ActivitySeverity.INFO);
    }
    
    /**
     * Get user activity history
     */
    public Page<UserActivityDTO> getUserActivityHistory(Long userId, Pageable pageable) {
        Page<UserActivity> activities = userActivityRepository
            .findByUserIdOrderByActivityTimestampDesc(userId, pageable);
        return activities.map(this::convertToDTO);
    }
    
    /**
     * Get user sessions
     */
    public List<UserSessionDTO> getUserSessions(Long userId) {
        List<UserSession> sessions = userSessionRepository
            .findByUserIdOrderByLoginTimeDesc(userId);
        return sessions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Terminate user session
     */
    @Transactional
    public void terminateUserSession(Long sessionId, User adminUser) {
        UserSession session = userSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        
        session.setStatus(SessionStatus.TERMINATED);
        session.setIsActive(false);
        session.setLogoutTime(LocalDateTime.now());
        session.setTerminationReason("Terminated by admin: " + adminUser.getUsername());
        
        userSessionRepository.save(session);
        
        // Log activity
        logUserActivity(session.getUser(), ActivityType.LOGOUT, 
                       "Session terminated by admin: " + adminUser.getUsername(),
                       ActivitySeverity.WARNING);
    }
    
    /**
     * Send email verification
     */
    private void sendEmailVerification(User user) {
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(48));
        userRepository.save(user);
        
        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), verificationToken);
    }
    
    /**
     * Log user activity
     */
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
    
    /**
     * Create user notification
     */
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
        // Generate secure temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}
```

### API Endpoints

#### UserManagementController
```java
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {
    
    @Autowired
    private UserManagementService userManagementService;
    
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
        User adminUser = userService.findByUsername(authentication.getName());
        User user = userManagementService.createUser(request, adminUser);
        return ResponseEntity.ok(convertToDTO(user));
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId,
                                            @RequestBody UpdateUserRequest request,
                                            Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        User user = userManagementService.updateUser(userId, request, adminUser);
        return ResponseEntity.ok(convertToDTO(user));
    }
    
    @PostMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long userId,
                                      @RequestBody BanUserRequest request,
                                      Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        userManagementService.banUser(userId, request, adminUser);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId,
                                        Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
        userManagementService.unbanUser(userId, adminUser);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long userId,
                                                 Authentication authentication) {
        User adminUser = userService.findByUsername(authentication.getName());
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
        User adminUser = userService.findByUsername(authentication.getName());
        userManagementService.terminateUserSession(sessionId, adminUser);
        return ResponseEntity.ok().build();
    }
}

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        userManagementService.requestPasswordReset(request.getEmail(), ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(
            @RequestBody CompletePasswordResetRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        userManagementService.completePasswordReset(
            request.getResetToken(), request.getNewPassword(), ipAddress, userAgent);
        
        return ResponseEntity.ok().build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### Frontend Implementation (fm-admin)

#### UserManagement Component
```jsx
import React, { useState, useEffect } from 'react';
import { getUsers, getUserDashboard, banUser, unbanUser, resetUserPassword } from '../services/api';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [dashboard, setDashboard] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({
        search: '',
        isActive: null,
        isVerified: null,
        isBanned: null,
        role: ''
    });
    const [pagination, setPagination] = useState({
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0
    });

    useEffect(() => {
        loadUserData();
    }, [filters, pagination.page]);

    const loadUserData = async () => {
        try {
            const [usersResponse, dashboardResponse] = await Promise.all([
                getUsers(pagination.page, pagination.size, filters),
                getUserDashboard()
            ]);
            
            setUsers(usersResponse.data.content);
            setPagination(prev => ({
                ...prev,
                totalElements: usersResponse.data.totalElements,
                totalPages: usersResponse.data.totalPages
            }));
            setDashboard(dashboardResponse.data);
        } catch (error) {
            console.error('Error loading user data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleBanUser = async (userId, reason, duration) => {
        try {
            await banUser(userId, { reason, banDuration: duration });
            loadUserData(); // Refresh data
        } catch (error) {
            console.error('Error banning user:', error);
        }
    };

    const handleUnbanUser = async (userId) => {
        try {
            await unbanUser(userId);
            loadUserData(); // Refresh data
        } catch (error) {
            console.error('Error unbanning user:', error);
        }
    };

    const handleResetPassword = async (userId) => {
        if (window.confirm('Are you sure you want to reset this user\'s password? They will receive a temporary password via email.')) {
            try {
                await resetUserPassword(userId);
                alert('Password reset successfully. User will receive temporary password via email.');
            } catch (error) {
                console.error('Error resetting password:', error);
            }
        }
    };

    const getStatusBadge = (user) => {
        if (user.isBanned) return <span className="badge badge-danger">Banned</span>;
        if (!user.isActive) return <span className="badge badge-secondary">Inactive</span>;
        if (!user.isVerified) return <span className="badge badge-warning">Unverified</span>;
        return <span className="badge badge-success">Active</span>;
    };

    if (loading) return <div className="loading">Loading user management...</div>;

    return (
        <div className="user-management">
            <div className="management-header">
                <h1>User Management</h1>
                <button className="btn-primary">Create New User</button>
            </div>

            {/* Dashboard Stats */}
            <div className="user-stats">
                <div className="stat-card">
                    <h3>Total Users</h3>
                    <span className="stat-number">{dashboard.totalUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>Active Users</h3>
                    <span className="stat-number">{dashboard.activeUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>Verified Users</h3>
                    <span className="stat-number">{dashboard.verifiedUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>Banned Users</h3>
                    <span className="stat-number">{dashboard.bannedUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>New (24h)</h3>
                    <span className="stat-number">{dashboard.newUsersLast24Hours}</span>
                </div>
                <div className="stat-card">
                    <h3>Active Sessions</h3>
                    <span className="stat-number">{dashboard.activeSessionsCount}</span>
                </div>
            </div>

            {/* Filters */}
            <div className="user-filters">
                <div className="filter-group">
                    <input
                        type="text"
                        placeholder="Search users..."
                        value={filters.search}
                        onChange={(e) => setFilters({...filters, search: e.target.value})}
                    />
                </div>
                <div className="filter-group">
                    <select
                        value={filters.isActive || ''}
                        onChange={(e) => setFilters({...filters, isActive: e.target.value ? e.target.value === 'true' : null})}
                    >
                        <option value="">All Status</option>
                        <option value="true">Active</option>
                        <option value="false">Inactive</option>
                    </select>
                </div>
                <div className="filter-group">
                    <select
                        value={filters.isVerified || ''}
                        onChange={(e) => setFilters({...filters, isVerified: e.target.value ? e.target.value === 'true' : null})}
                    >
                        <option value="">All Verification</option>
                        <option value="true">Verified</option>
                        <option value="false">Unverified</option>
                    </select>
                </div>
                <div className="filter-group">
                    <select
                        value={filters.isBanned || ''}
                        onChange={(e) => setFilters({...filters, isBanned: e.target.value ? e.target.value === 'true' : null})}
                    >
                        <option value="">All Ban Status</option>
                        <option value="false">Not Banned</option>
                        <option value="true">Banned</option>
                    </select>
                </div>
            </div>

            {/* Users Table */}
            <div className="users-table">
                <table>
                    <thead>
                        <tr>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Name</th>
                            <th>Status</th>
                            <th>Roles</th>
                            <th>Created</th>
                            <th>Last Login</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map(user => (
                            <tr key={user.id}>
                                <td>{user.username}</td>
                                <td>{user.email}</td>
                                <td>{user.firstName} {user.lastName}</td>
                                <td>{getStatusBadge(user)}</td>
                                <td>
                                    {user.roles.map(role => (
                                        <span key={role} className="role-badge">{role}</span>
                                    ))}
                                </td>
                                <td>{new Date(user.createdDate).toLocaleDateString()}</td>
                                <td>
                                    {user.lastLoginDate ? 
                                        new Date(user.lastLoginDate).toLocaleDateString() : 
                                        'Never'
                                    }
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button 
                                            className="btn-secondary btn-sm"
                                            onClick={() => setSelectedUser(user)}
                                        >
                                            View
                                        </button>
                                        
                                        {!user.isBanned ? (
                                            <button 
                                                className="btn-warning btn-sm"
                                                onClick={() => handleBanUser(user.id, 'Admin action', null)}
                                            >
                                                Ban
                                            </button>
                                        ) : (
                                            <button 
                                                className="btn-success btn-sm"
                                                onClick={() => handleUnbanUser(user.id)}
                                            >
                                                Unban
                                            </button>
                                        )}
                                        
                                        <button 
                                            className="btn-info btn-sm"
                                            onClick={() => handleResetPassword(user.id)}
                                        >
                                            Reset Password
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            <div className="pagination">
                <button 
                    disabled={pagination.page === 0}
                    onClick={() => setPagination(prev => ({...prev, page: prev.page - 1}))}
                >
                    Previous
                </button>
                <span>
                    Page {pagination.page + 1} of {pagination.totalPages}
                </span>
                <button 
                    disabled={pagination.page >= pagination.totalPages - 1}
                    onClick={() => setPagination(prev => ({...prev, page: prev.page + 1}))}
                >
                    Next
                </button>
            </div>

            {/* User Detail Modal */}
            {selectedUser && (
                <UserDetailModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onUpdate={loadUserData}
                />
            )}
        </div>
    );
};

const UserDetailModal = ({ user, onClose, onUpdate }) => {
    const [activities, setActivities] = useState([]);
    const [sessions, setSessions] = useState([]);
    const [activeTab, setActiveTab] = useState('profile');

    useEffect(() => {
        if (activeTab === 'activities') {
            loadUserActivities();
        } else if (activeTab === 'sessions') {
            loadUserSessions();
        }
    }, [activeTab, user.id]);

    const loadUserActivities = async () => {
        try {
            const response = await getUserActivities(user.id);
            setActivities(response.data.content);
        } catch (error) {
            console.error('Error loading user activities:', error);
        }
    };

    const loadUserSessions = async () => {
        try {
            const response = await getUserSessions(user.id);
            setSessions(response.data);
        } catch (error) {
            console.error('Error loading user sessions:', error);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content large">
                <div className="modal-header">
                    <h2>User Details - {user.username}</h2>
                    <button onClick={onClose}>×</button>
                </div>
                
                <div className="modal-tabs">
                    <button 
                        className={activeTab === 'profile' ? 'active' : ''}
                        onClick={() => setActiveTab('profile')}
                    >
                        Profile
                    </button>
                    <button 
                        className={activeTab === 'activities' ? 'active' : ''}
                        onClick={() => setActiveTab('activities')}
                    >
                        Activities
                    </button>
                    <button 
                        className={activeTab === 'sessions' ? 'active' : ''}
                        onClick={() => setActiveTab('sessions')}
                    >
                        Sessions
                    </button>
                </div>

                <div className="modal-body">
                    {activeTab === 'profile' && (
                        <div className="user-profile">
                            <div className="profile-section">
                                <h3>Basic Information</h3>
                                <div className="profile-grid">
                                    <div className="profile-item">
                                        <label>Username:</label>
                                        <span>{user.username}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Email:</label>
                                        <span>{user.email}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Full Name:</label>
                                        <span>{user.firstName} {user.lastName}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Phone:</label>
                                        <span>{user.phoneNumber || 'Not provided'}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Country:</label>
                                        <span>{user.country || 'Not provided'}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Language:</label>
                                        <span>{user.preferredLanguage || 'Not set'}</span>
                                    </div>
                                </div>
                            </div>

                            <div className="profile-section">
                                <h3>Account Status</h3>
                                <div className="profile-grid">
                                    <div className="profile-item">
                                        <label>Status:</label>
                                        <span className={`status ${user.isActive ? 'active' : 'inactive'}`}>
                                            {user.isActive ? 'Active' : 'Inactive'}
                                        </span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Verified:</label>
                                        <span className={`status ${user.isVerified ? 'verified' : 'unverified'}`}>
                                            {user.isVerified ? 'Verified' : 'Unverified'}
                                        </span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Banned:</label>
                                        <span className={`status ${user.isBanned ? 'banned' : 'not-banned'}`}>
                                            {user.isBanned ? 'Yes' : 'No'}
                                        </span>
                                    </div>
                                    {user.isBanned && (
                                        <>
                                            <div className="profile-item">
                                                <label>Ban Reason:</label>
                                                <span>{user.banReason}</span>
                                            </div>
                                            <div className="profile-item">
                                                <label>Banned Until:</label>
                                                <span>
                                                    {user.bannedUntil ? 
                                                        new Date(user.bannedUntil).toLocaleString() : 
                                                        'Permanent'
                                                    }
                                                </span>
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>

                            <div className="profile-section">
                                <h3>Account History</h3>
                                <div className="profile-grid">
                                    <div className="profile-item">
                                        <label>Created:</label>
                                        <span>{new Date(user.createdDate).toLocaleString()}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Last Login:</label>
                                        <span>
                                            {user.lastLoginDate ? 
                                                new Date(user.lastLoginDate).toLocaleString() : 
                                                'Never'
                                            }
                                        </span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Failed Login Attempts:</label>
                                        <span>{user.failedLoginAttempts}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Password Changed:</label>
                                        <span>
                                            {user.passwordChangedDate ? 
                                                new Date(user.passwordChangedDate).toLocaleString() : 
                                                'Never'
                                            }
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {activeTab === 'activities' && (
                        <div className="user-activities">
                            <h3>Recent Activities</h3>
                            <div className="activities-list">
                                {activities.map(activity => (
                                    <div key={activity.id} className="activity-item">
                                        <div className="activity-icon">
                                            <i className={getActivityIcon(activity.activityType)}></i>
                                        </div>
                                        <div className="activity-content">
                                            <div className="activity-description">
                                                {activity.activityDescription}
                                            </div>
                                            <div className="activity-meta">
                                                <span className="activity-time">
                                                    {new Date(activity.activityTimestamp).toLocaleString()}
                                                </span>
                                                <span className={`activity-severity ${activity.severity.toLowerCase()}`}>
                                                    {activity.severity}
                                                </span>
                                                {activity.ipAddress && (
                                                    <span className="activity-ip">
                                                        IP: {activity.ipAddress}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {activeTab === 'sessions' && (
                        <div className="user-sessions">
                            <h3>User Sessions</h3>
                            <div className="sessions-list">
                                {sessions.map(session => (
                                    <div key={session.id} className="session-item">
                                        <div className="session-info">
                                            <div className="session-device">
                                                <strong>{session.deviceInfo || 'Unknown Device'}</strong>
                                            </div>
                                            <div className="session-details">
                                                <span>IP: {session.ipAddress}</span>
                                                <span>Location: {session.location || 'Unknown'}</span>
                                                <span>Login: {new Date(session.loginTime).toLocaleString()}</span>
                                                {session.lastActivityTime && (
                                                    <span>Last Activity: {new Date(session.lastActivityTime).toLocaleString()}</span>
                                                )}
                                            </div>
                                        </div>
                                        <div className="session-status">
                                            <span className={`status-badge ${session.status.toLowerCase()}`}>
                                                {session.status}
                                            </span>
                                            {session.isActive && (
                                                <button 
                                                    className="btn-danger btn-sm"
                                                    onClick={() => terminateSession(session.id)}
                                                >
                                                    Terminate
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

const getActivityIcon = (activityType) => {
    switch (activityType) {
        case 'LOGIN': return 'fas fa-sign-in-alt';
        case 'LOGOUT': return 'fas fa-sign-out-alt';
        case 'PASSWORD_CHANGE': return 'fas fa-key';
        case 'PROFILE_UPDATE': return 'fas fa-user-edit';
        default: return 'fas fa-info-circle';
    }
};

export default UserManagement;
```

## Implementation Notes

1. **Comprehensive User Tracking**: Track all user activities, sessions, and account changes
2. **Password Reset Flow**: Secure password reset with email verification and token expiry
3. **Account Security**: Ban/unban functionality with reason tracking and notifications
4. **Session Management**: Track and manage user sessions with termination capabilities
5. **Activity Logging**: Detailed logging of all user activities for audit purposes
6. **Email Notifications**: Automated emails for account changes and security events

## Dependencies

- Email service for notifications and password resets
- JWT authentication system
- User role and permission system
- Logging framework for audit trails
- IP geolocation service for session tracking

## Testing Strategy

1. **Unit Tests**: Test all user management service methods
2. **Integration Tests**: Test complete user management workflows
3. **Security Tests**: Test password reset security and session management
4. **Email Tests**: Test all email notification scenarios
5. **Performance Tests**: Test with large numbers of users and activities