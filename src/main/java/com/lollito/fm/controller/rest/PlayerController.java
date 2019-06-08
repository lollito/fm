package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.rest.PlayerCondition;
import com.lollito.fm.service.ClubService;
import com.lollito.fm.service.PlayerService;

@RestController
@RequestMapping(value="/player")
public class PlayerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ClubService clubService;
	@Autowired private PlayerService playerService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Player> players() {
        return clubService.load().getTeam().getPlayers();
    }
   
	
	@RequestMapping(value = "/condition", method = RequestMethod.GET)
    public List<PlayerCondition> condition() {
        return playerService.findAllCondition();
    }
}
