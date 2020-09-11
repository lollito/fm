package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.rest.PlayerCondition;
import com.lollito.fm.service.PlayerService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping(value="/api/player")
public class PlayerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private UserService userService;
	@Autowired private PlayerService playerService;
	
	@GetMapping(value = "/")
    public List<Player> players() {
        return userService.getLoggedUser().getClub().getTeam().getPlayers();
    }
   
	
	@GetMapping(value = "/condition")
    public List<PlayerCondition> condition() {
        return playerService.findAllCondition();
    }
	
	@GetMapping(value = "/onSale")
	public List<Player> getOnSale() {
		return playerService.findByOnSale(Boolean.TRUE);
	}
	
	@PostMapping(value = "/{id}/onSale")
	public Player onSale(@PathVariable(value="id") Long id) {
		return playerService.onSale(id);
	}
}
