package com.lollito.fm.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.lollito.fm.controller.rest.errors.GameNotFoundException;
import com.lollito.fm.controller.rest.errors.CustomParameterizedException;
import com.lollito.fm.model.Game;
import com.lollito.fm.repository.rest.GameRepository;

@RunWith(MockitoJUnitRunner.class)
public class SessionBeanTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private SessionBean sessionBean;

    @InjectMocks
    private SessionBean sessionBean;

    @Mock
    private GameRepository gameRepository;

    @Before
    public void setUp() {
    }

    @Test(expected = GameNotFoundException.class)
    public void getGame_throwsException_whenGameIdIsNull() {}
    @Test(expected = CustomParameterizedException.class)
    public void getGame_shouldThrowException_whenGameIdIsNull() {
        sessionBean.setGameId(null);
        sessionBean.getGame();
    }

    @Test(expected = GameNotFoundException.class)
    public void getGame_throwsException_whenGameNotFound() {
        Long gameId = 1L;
        sessionBean.setGameId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
    }
    @Test(expected = CustomParameterizedException.class)
    public void getGame_shouldThrowException_whenGameNotFound() {
        Long gameId = 1L;
        sessionBean.setGameId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        sessionBean.getGame();
    }

    @Test
    public void getGame_returnsGame_whenFound() {
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);

        sessionBean.setGameId(gameId);
    }
    public void getGame_shouldReturnGame_whenFound() {
        Long gameId = 1L;
        sessionBean.setGameId(gameId);
        Game game = new Game();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Game result = sessionBean.getGame();

        assertNotNull(result);
        assertEquals(gameId, result.getId());
    }
}
