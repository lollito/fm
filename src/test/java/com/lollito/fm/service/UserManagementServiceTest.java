package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.User;
import com.lollito.fm.model.UserSession;
import com.lollito.fm.model.dto.BanUserRequest;
import com.lollito.fm.model.SessionStatus;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.UserSessionRepository;
import com.lollito.fm.repository.rest.UserActivityRepository;
import com.lollito.fm.repository.rest.UserNotificationRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserActivity;
import com.lollito.fm.model.UserNotification;
import com.lollito.fm.model.dto.CreateUserRequest;
import com.lollito.fm.repository.rest.PasswordResetRequestRepository;
import com.lollito.fm.repository.rest.UserActivityRepository;
import com.lollito.fm.repository.rest.UserNotificationRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.UserSessionRepository;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceTest {

    @InjectMocks
    private UserManagementService userManagementService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    void banUser_Success_WithDuration() {
        // Arrange
        Long userId = 1L;
        User adminUser = User.builder().username("admin").build();
        User targetUser = User.builder().id(userId).username("target").isBanned(false).build();

        BanUserRequest request = new BanUserRequest();
        request.setReason("Violation of rules");
        request.setBanDuration(Duration.ofDays(7));

        UserSession activeSession = UserSession.builder()
            .id(100L)
            .user(targetUser)
            .isActive(true)
            .status(SessionStatus.ACTIVE)
            .build();

        List<UserSession> sessions = Collections.singletonList(activeSession);

        when(userRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(userSessionRepository.findByUserIdOrderByLoginTimeDesc(userId)).thenReturn(sessions);

        // Act
        userManagementService.banUser(userId, request, adminUser);

        // Assert
        // Verify user update
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getIsBanned()).isTrue();
        assertThat(savedUser.getBanReason()).isEqualTo("Violation of rules");
        assertThat(savedUser.getBannedBy()).isEqualTo("admin");
        assertThat(savedUser.getBannedUntil()).isAfter(LocalDateTime.now());

        // Verify session termination
        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(sessionCaptor.capture());
        UserSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getIsActive()).isFalse();
        assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.TERMINATED);
        assertThat(savedSession.getTerminationReason()).isEqualTo("User banned");

        // Verify email sent
        verify(emailService).sendBanNotificationEmail(targetUser, "Violation of rules");

        // Verify notification created
        verify(userNotificationRepository).save(any());

        // Verify activity logged
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtils jwtUtils;

    @Test
    public void resetUserPassword_Success() {
        Long userId = 1L;
        User adminUser = User.builder().username("admin").build();
        User targetUser = User.builder().id(userId).username("target").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        userManagementService.resetUserPassword(userId, adminUser);

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(passwordCaptor.capture());
        String encodedPasswordInput = passwordCaptor.getValue();

        ArgumentCaptor<String> emailPasswordCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendTemporaryPasswordEmail(eq(targetUser), emailPasswordCaptor.capture());
        String emailPasswordInput = emailPasswordCaptor.getValue();

        assertEquals(encodedPasswordInput, emailPasswordInput, "Password sent in email should match password encoded");

        assertEquals("encoded_password", targetUser.getPassword());
        assertNotNull(targetUser.getPasswordChangedDate());
        assertNotNull(targetUser.getLastModifiedDate());
        assertEquals("admin", targetUser.getLastModifiedBy());

        verify(userRepository).save(targetUser);
        verify(userNotificationRepository).save(any(UserNotification.class));
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    public void resetUserPassword_UserNotFound() {
        Long userId = 1L;
        User adminUser = User.builder().username("admin").build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userManagementService.resetUserPassword(userId, adminUser);
        });
    }
    @Test
    public void testCreateUser_Success_DefaultValues() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setCountry("Italy");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        // Default isActive = true (implied null in request handled in service), isVerified = false (implied null)

        User adminUser = new User();
        adminUser.setUsername("admin");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userManagementService.createUser(request, adminUser);

        // Assert
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("encodedPassword", createdUser.getPassword());

        // save is called once for creation, and once inside sendEmailVerification
        verify(userRepository, times(2)).save(any(User.class));
        verify(emailService).sendEmailVerification(anyString(), anyString(), anyString());
        verify(userActivityRepository).save(any());
    }

    @Test
    public void testCreateUser_Success_Verified() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setIsActive(true);
        request.setIsVerified(true);

        User adminUser = new User();
        adminUser.setUsername("admin");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userManagementService.createUser(request, adminUser);

        // Assert
        assertNotNull(createdUser);
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService).sendWelcomeEmail(any(User.class));
        verify(userActivityRepository).save(any());
    }

    @Test
    void banUser_Success_NoDuration() {
        // Arrange
        Long userId = 1L;
        User adminUser = User.builder().username("admin").build();
        User targetUser = User.builder().id(userId).username("target").isBanned(false).build();

        BanUserRequest request = new BanUserRequest();
        request.setReason("Permanent ban");
        request.setBanDuration(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(targetUser));
        when(userSessionRepository.findByUserIdOrderByLoginTimeDesc(userId)).thenReturn(Collections.emptyList());

        // Act
        userManagementService.banUser(userId, request, adminUser);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getIsBanned()).isTrue();
        assertThat(savedUser.getBanReason()).isEqualTo("Permanent ban");
        assertThat(savedUser.getBannedBy()).isEqualTo("admin");
        assertThat(savedUser.getBannedUntil()).isNull(); // Should be null if no duration provided
    }

    @Test
    void banUser_UserNotFound() {
        // Arrange
        Long userId = 999L;
        User adminUser = User.builder().username("admin").build();
        BanUserRequest request = new BanUserRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userManagementService.banUser(userId, request, adminUser))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("User not found");

        verify(userRepository, times(0)).save(any(User.class));
        verify(emailService, times(0)).sendBanNotificationEmail(any(), anyString());
    public void testCreateUser_Success_Inactive() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setIsActive(false);

        User adminUser = new User();
        adminUser.setUsername("admin");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userManagementService.createUser(request, adminUser);

        // Assert
        assertNotNull(createdUser);
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoInteractions(emailService);
        verify(userActivityRepository).save(any());
    }

    @Test
    public void testCreateUser_UsernameExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existingUser");

        User adminUser = new User();
        adminUser.setUsername("admin");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userManagementService.createUser(request, adminUser);
        });
        assertEquals("Username already exists", exception.getMessage());

        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testCreateUser_EmailExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("existing@example.com");

        User adminUser = new User();
        adminUser.setUsername("admin");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userManagementService.createUser(request, adminUser);
        });
        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, times(0)).save(any(User.class));
    }
}
