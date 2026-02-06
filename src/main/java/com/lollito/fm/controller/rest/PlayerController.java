package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.mapper.PlayerMapper;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.PlayerCondition;
import com.lollito.fm.service.PlayerService;
import com.lollito.fm.service.TeamService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping(value="/api/player")
public class PlayerController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private UserService userService;
	@Autowired private PlayerService playerService;
	@Autowired private TeamService teamService;
	@Autowired private PlayerMapper playerMapper;
	
	@GetMapping(value = "/")
    public List<PlayerDTO> players(@RequestParam(required = false) Long teamId) {
        if(teamId != null) {
            return teamService.findById(teamId).getPlayers().stream()
                .map(playerMapper::toDto)
                .collect(Collectors.toList());
        }
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
		checkOwnership(id);
		return playerMapper.toDto(playerService.onSale(id));
	}

	@PostMapping(value = "/{id}/change-role")
	public PlayerDTO changeRole(@PathVariable(value="id") Long id, @RequestParam(value="role") Integer role) {
		checkOwnership(id);
		return playerMapper.toDto(playerService.changeRole(id, role));
	}

	private void checkOwnership(Long playerId) {
        Player player = playerService.findOne(playerId);
        User user = userService.getLoggedUser();
        if(!player.getTeam().getClub().getUser().getId().equals(user.getId())){
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this player");
        }
    }
}
