package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void delete_whenUserIsSuperAdmin_shouldDeleteGame() {
        Long gameId = 1L;
        User admin = new User();
        admin.setAdminRole(AdminRole.SUPER_ADMIN);

        Game game = new Game();
        game.setId(gameId);

        when(userService.getLoggedUser()).thenReturn(admin);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.delete(gameId);

        verify(gameRepository).deleteById(gameId);
    }

    @Test
    void delete_whenUserIsOwner_shouldDeleteGame() {
        Long gameId = 1L;
        User owner = new User();
        owner.setId(100L);
        owner.setUsername("owner");

        Game game = new Game();
        game.setId(gameId);
        game.setOwner(owner);

        when(userService.getLoggedUser()).thenReturn(owner);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.delete(gameId);

        verify(gameRepository).deleteById(gameId);
    }

    @Test
    void delete_whenUserIsNeitherOwnerNorAdmin_shouldThrowAccessDeniedException() {
        Long gameId = 1L;
        User otherUser = new User();
        otherUser.setId(200L);
        otherUser.setUsername("other");

        User owner = new User();
        owner.setId(100L);
        owner.setUsername("owner");

        Game game = new Game();
        game.setId(gameId);
        game.setOwner(owner);

        when(userService.getLoggedUser()).thenReturn(otherUser);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        assertThrows(AccessDeniedException.class, () -> gameService.delete(gameId));

        verify(gameRepository, never()).deleteById(anyLong());
    }
}
