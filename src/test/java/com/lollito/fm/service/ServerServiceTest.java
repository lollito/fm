package com.lollito.fm.service;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.repository.rest.ServerRepository;

import org.springframework.security.access.AccessDeniedException;

import com.lollito.fm.model.AdminRole;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.User;

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
}
