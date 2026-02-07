package com.lollito.fm.controller.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.mapper.FormationMapper;
import com.lollito.fm.model.dto.FormationDTO;
import com.lollito.fm.model.rest.FormationRequest;
import com.lollito.fm.service.FormationService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping(value="/api/formation")
public class FormationController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private FormationService formationService;
	@Autowired private UserService userService;
	@Autowired private FormationMapper formationMapper;

	@GetMapping(value = "/")
	public FormationDTO get() {
		return formationMapper.toDto(userService.getLoggedUser().getClub().getTeam().getFormation());
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
    public FormationDTO create(FormationRequest formationRequest) {
		return formationMapper.toDto(formationService.createPlayerFormation(formationRequest));
    }

	@RequestMapping(value = "/auto", method = RequestMethod.GET)
    public FormationDTO auto(Model model) {
		return formationMapper.toDto(formationService.createPlayerFormation());
    }

   
}
