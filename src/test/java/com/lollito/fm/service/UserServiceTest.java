package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.repository.rest.RoleRepository;
import com.lollito.fm.repository.rest.ServerRepository;
import com.lollito.fm.repository.rest.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubService clubService;

    @Mock
    private NewsService newsService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext(Object principal) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetLoggedUser_WithUserDetails() {
        // Arrange
        String username = "testUser";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        setupSecurityContext(userDetails);

        User expectedUser = new User();
        expectedUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.getLoggedUser();

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testGetLoggedUser_WithStringPrincipal() {
        // Arrange
        String username = "testUser";
        setupSecurityContext(username);

        User expectedUser = new User();
        expectedUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.getLoggedUser();

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testGetLoggedUser_UserNotFound() {
        // Arrange
        String username = "unknownUser";
        setupSecurityContext(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getLoggedUser());
        assertEquals("User '" + username + "' non trovato", exception.getMessage());
    }
}
