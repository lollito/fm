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
import com.lollito.fm.utils.RandomUtils;

@RestController
@RequestMapping(value="/player")
public class PlayerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ClubService clubService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Player> players() {
        return clubService.load().getTeam().getPlayers();
    }
   
	@RequestMapping(value = "/condition", method = RequestMethod.GET)
    public String condition(Model model) {
		int azioni = RandomUtils.randomValue(10,20);
		List<Player> players =  clubService.load().getTeam().getPlayers();
		for (int i = 0; i < azioni; i++) {
			for(Player player : players) {
	        	double d = -((10 * player.getStamina())/99) + (1000/99);
	        	player.decrementCondition(d/azioni);
	        }
		}
		for(Player player : players) {
        	logger.info("condition - {}", player.getCondition());
        }
        return "ok";
    }
}
