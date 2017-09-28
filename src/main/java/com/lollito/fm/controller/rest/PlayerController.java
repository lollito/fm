package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Player;
import com.lollito.fm.service.ClubService;

@RestController
@RequestMapping(value="/player")
public class PlayerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ClubService clubService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Player> game(Model model) {
        return clubService.load().getTeam().getPlayers();
    }
   
}
