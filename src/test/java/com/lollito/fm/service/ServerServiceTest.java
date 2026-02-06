package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.ServerResponse;
import com.lollito.fm.repository.rest.ServerRepository;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ServerService serverService;

    @Test
    void testDeleteAll() {
        serverService.deleteAll();
        verify(serverRepository).deleteAll();
    }

    @Test
    void testLoadById_Success() {
        Long serverId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Server server = new Server();
        server.setId(serverId);
        server.setCurrentDate(now);

        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        ServerResponse response = serverService.load(serverId);

        assertNotNull(response);
        assertEquals(now, response.getCurrentDate());
    }

    @Test
    void testLoadById_NotFound() {
        Long serverId = 1L;
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serverService.load(serverId));
    }

    @Test
    void testLoadCurrent_Success() {
        User user = new User();
        Server server = new Server();
        LocalDateTime now = LocalDateTime.now();
        server.setCurrentDate(now);
        user.setServer(server);

        when(userService.getLoggedUser()).thenReturn(user);

        ServerResponse response = serverService.load();

        assertNotNull(response);
        assertEquals(now, response.getCurrentDate());
    }

    @Test
    void testLoadCurrent_NoServer() {
        User user = new User();
        user.setServer(null);

        when(userService.getLoggedUser()).thenReturn(user);

        assertThrows(EntityNotFoundException.class, () -> serverService.load());
    }
}
