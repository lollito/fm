package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    public GameResponse game(Model model, String clubName, String gameName) {
		Game game = gameService.create(clubName, gameName);
        return new GameResponse(game.getCurrentDate());
    }
   
	@RequestMapping(value = "/next", method = RequestMethod.PUT)
    public GameResponse next(Model model) {
		return gameService.next();
    }
	
	@RequestMapping(value = "/load", method = RequestMethod.GET)
    public GameResponse load(Model model, Long GameId) {
		return gameService.load(GameId);
    }
}
