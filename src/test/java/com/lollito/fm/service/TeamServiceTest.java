package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Team;
import com.lollito.fm.repository.rest.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private NameService nameService;

    @InjectMocks
    private TeamService teamService;

    @Test
    void findById_ShouldThrowException_WhenTeamNotFound() {
        // Arrange
        when(teamRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> teamService.findById(1L));
        assertEquals("Team not found", exception.getMessage());

        verify(teamRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnTeam_WhenTeamFound() {
        // Arrange
        Long teamId = 1L;
        Team mockTeam = new Team();
        mockTeam.setId(teamId);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(mockTeam));

        // Act
        Team result = teamService.findById(teamId);

        // Assert
        assertNotNull(result);
        assertEquals(teamId, result.getId());
        assertEquals(mockTeam, result);

        verify(teamRepository).findById(teamId);
    }
}
