package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.News;
import com.lollito.fm.model.Role;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.RegistrationRequest;
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
    private RegistrationRequest registrationRequest;
    private Country country;
    private Server server;
    private Club club;
    private Role role;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setName("Test");
        registrationRequest.setSurname("User");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password");
        registrationRequest.setCountryId(1L);
        registrationRequest.setClubName("Test Club");

        country = new Country();
        country.setId(1L);

        server = new Server();
        server.setId(10L);

        club = new Club();
        club.setName("Old Club Name");

        role = new Role();
        role.setName("ROLE_USER");
    }

    @Test
    void save_Success() {
        // Arrange
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(countryRepository.findById(registrationRequest.getCountryId())).thenReturn(Optional.of(country));
        when(bCryptPasswordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");

        // Use default server selection logic (no server ID in request)
        when(serverRepository.findAll()).thenReturn(List.of(server));

        when(clubService.findTopByLeagueServerAndLeagueCountryAndUserIsNull(server, country)).thenReturn(club);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = userService.save(registrationRequest);

        // Assert
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(country, savedUser.getCountry());
        assertEquals(server, savedUser.getServer());
        assertEquals(club, savedUser.getClub());
        assertEquals("Test Club", savedUser.getClub().getName());
        assertEquals(true, savedUser.getActive());

        verify(userRepository).existsByUsername("testuser");
        verify(countryRepository).findById(1L);
        verify(bCryptPasswordEncoder).encode("password");
        verify(serverRepository).findAll();
        verify(clubService).findTopByLeagueServerAndLeagueCountryAndUserIsNull(server, country);
        verify(clubService).save(club);
        verify(roleRepository).findByName("ROLE_USER");
        verify(newsService).save(any(News.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void save_UsernameExists() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.save(registrationRequest));
        assertEquals("Username alredy exist", exception.getMessage());
    }

    @Test
    void save_CountryNotFound() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(countryRepository.findById(registrationRequest.getCountryId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.save(registrationRequest));
    }

    @Test
    void save_ServerNotFound_WithId() {
        registrationRequest.setServerId(99L);

        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(countryRepository.findById(registrationRequest.getCountryId())).thenReturn(Optional.of(country));
        when(bCryptPasswordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(serverRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.save(registrationRequest));
        assertEquals("Server not found", exception.getMessage());
    }

    @Test
    void save_NoServersAvailable_WithoutId() {
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(countryRepository.findById(registrationRequest.getCountryId())).thenReturn(Optional.of(country));
        when(bCryptPasswordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(serverRepository.findAll()).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.save(registrationRequest));
        assertEquals("No servers available", exception.getMessage());
    }

    @Test
    void save_DefaultServerSelection() {
        // Arrange
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(countryRepository.findById(registrationRequest.getCountryId())).thenReturn(Optional.of(country));
        when(bCryptPasswordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");

        Server server1 = new Server();
        server1.setId(1L);
        Server server2 = new Server();
        server2.setId(2L);

        when(serverRepository.findAll()).thenReturn(List.of(server1, server2));

        when(clubService.findTopByLeagueServerAndLeagueCountryAndUserIsNull(server1, country)).thenReturn(club);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = userService.save(registrationRequest);

        // Assert
        assertEquals(server1, savedUser.getServer());
        verify(serverRepository).findAll();
    }
}
