package com.lollito.fm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.lollito.fm.bean.SessionBean;
import com.lollito.fm.controller.rest.errors.GameNotFoundException;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.rest.GameResponse;
import com.lollito.fm.repository.rest.GameRepository;

@RunWith(MockitoJUnitRunner.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SessionBean sessionBean;

    @InjectMocks
    private GameService gameService;

    @Test
    public void testLoadGameSuccess() {
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);
        game.setCurrentDate(LocalDate.now());

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        GameResponse response = gameService.load(gameId);

        assertEquals(game.getCurrentDate(), response.getCurrentDate());
        verify(sessionBean, times(1)).setGameId(gameId);
    }

    @Test(expected = GameNotFoundException.class)
    public void testLoadGameNotFound() {
        Long gameId = 1L;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        gameService.load(gameId);
    }
}
