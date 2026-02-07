package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

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
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtils jwtUtils;

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
