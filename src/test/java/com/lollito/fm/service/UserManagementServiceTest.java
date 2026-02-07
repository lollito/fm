package com.lollito.fm.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserActivity;
import com.lollito.fm.model.UserNotification;
import com.lollito.fm.repository.rest.PasswordResetRequestRepository;
import com.lollito.fm.repository.rest.UserActivityRepository;
import com.lollito.fm.repository.rest.UserNotificationRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.UserSessionRepository;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserManagementService userManagementService;

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
}
