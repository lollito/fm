package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Player;
import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.model.rest.PlayerCondition;
import com.lollito.fm.service.PlayerService;
import com.lollito.fm.service.UserService;
import com.lollito.fm.mapper.PlayerMapper;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/player")
public class PlayerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private UserService userService;
	@Autowired private PlayerService playerService;
	@Autowired private PlayerMapper playerMapper;
	
	@GetMapping(value = "/")
    public List<PlayerDTO> players() {
        return userService.getLoggedUser().getClub().getTeam().getPlayers().stream()
			.map(playerMapper::toDto)
			.collect(Collectors.toList());
    }
   
	
	@GetMapping(value = "/condition")
    public List<PlayerCondition> condition() {
        return playerService.findAllCondition();
    }
	
	@GetMapping(value = "/onSale")
	public List<PlayerDTO> getOnSale() {
		return playerService.findByOnSale(Boolean.TRUE).stream()
			.map(playerMapper::toDto)
			.collect(Collectors.toList());
	}
	
	@PostMapping(value = "/{id}/onSale")
	public PlayerDTO onSale(@PathVariable(value="id") Long id) {
		return playerMapper.toDto(playerService.onSale(id));
	}

	@PostMapping(value = "/{id}/change-role")
	public PlayerDTO changeRole(@PathVariable(value="id") Long id, @RequestParam(value="role") Integer role) {
		return playerMapper.toDto(playerService.changeRole(id, role));
	}
}
