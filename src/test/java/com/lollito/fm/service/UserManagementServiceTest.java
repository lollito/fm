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
    }
}
