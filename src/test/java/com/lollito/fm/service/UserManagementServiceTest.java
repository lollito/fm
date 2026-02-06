package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.SessionStatus;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserSession;
import com.lollito.fm.model.dto.BanUserRequest;
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
    public void testBanUser_NPlus1() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .isActive(true)
                .build();
        User adminUser = User.builder().username("admin").build();

        BanUserRequest request = new BanUserRequest();
        request.setReason("Violation");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserSession session1 = UserSession.builder().id(101L).isActive(true).build();
        UserSession session2 = UserSession.builder().id(102L).isActive(true).build();
        UserSession session3 = UserSession.builder().id(103L).isActive(false).build(); // Inactive one

        List<UserSession> sessions = Arrays.asList(session1, session2, session3);

        when(userSessionRepository.findByUserIdOrderByLoginTimeDesc(userId)).thenReturn(sessions);

        userManagementService.banUser(userId, request, adminUser);

        // Verify that saveAll is called once
        verify(userSessionRepository, times(1)).saveAll(any());
        // Verify that save is NOT called (except potentially for other things not in this flow, but here we strictly check N+1)
        verify(userSessionRepository, times(0)).save(any(UserSession.class));
    }
}
