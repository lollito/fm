package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.bean.SessionBean;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.service.GameService;

@RestController
@RequestMapping(value="/game")
public class GameController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private GameService gameService;
	@Autowired private SessionBean sessionBean;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
    public String game(Model model, Club club) {
		gameService.create();
        return "ok";
    }
   
	@RequestMapping(value = "/", method = RequestMethod.PUT)
    public String next(Model model, Club club) {
		Game game = sessionBean.getGame();
		if (game == null){
			//TODO error
		} else {
			
		}
        return "ok";
    }
}
