package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Game;
import com.lollito.fm.model.rest.GameResponse;
import com.lollito.fm.service.GameService;

@RestController
@RequestMapping(value="/game")
public class GameController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private GameService gameService;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
    public GameResponse create(@RequestParam(required = true) String gameName) {
		Game game = gameService.create(gameName);
        return new GameResponse(game.getCurrentDate());
    }
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public GameResponse game() {
		return gameService.load();
    }
	
	@RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public List<Game> findAll() {
		return gameService.findAll();
    }
	
	@RequestMapping(value = "/next", method = RequestMethod.POST)
    public GameResponse next() {
		return gameService.next();
    }
	
	@RequestMapping(value = "/load", method = RequestMethod.GET)
    public GameResponse load(Long gameId) {
		return gameService.load(gameId);
    }
	
	@RequestMapping(value = "/", method = RequestMethod.DELETE)
    public String delete(Long gameId) {
		gameService.delete(gameId);
		return "ok";
    }
	
}
