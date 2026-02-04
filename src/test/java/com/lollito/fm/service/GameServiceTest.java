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

import com.lollito.fm.repository.rest.GameRepository;

import org.springframework.security.access.AccessDeniedException;

import com.lollito.fm.model.AdminRole;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.GameRepository;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GameService gameService;

    @Test
    void testDeleteAll() {
        gameService.deleteAll();
        verify(gameRepository).deleteAll();
    }
}
