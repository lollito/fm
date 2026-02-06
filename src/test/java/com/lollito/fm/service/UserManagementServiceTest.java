package com.lollito.fm.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.CreateUserRequest;
import com.lollito.fm.repository.rest.PasswordResetRequestRepository;
import com.lollito.fm.repository.rest.UserActivityRepository;
import com.lollito.fm.repository.rest.UserNotificationRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.repository.rest.UserSessionRepository;

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
    void testCreateUser_DuplicateEmail() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testUser");
        request.setEmail("existing@example.com");

        User adminUser = new User();
        adminUser.setUsername("admin");

        // Mock userRepository behavior
        // Since existsByUsername is called first, we need to make sure it returns false
        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        // Then existsByEmail is called and returns true
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userManagementService.createUser(request, adminUser);
        });

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}
