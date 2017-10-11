package com.lollito.fm.controller.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Formation;
import com.lollito.fm.service.ClubService;
import com.lollito.fm.service.FormationService;

@RestController
@RequestMapping(value="/formation")
public class FormationController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private FormationService formationService;
	@Autowired private ClubService clubService;
	
	@RequestMapping(value = "/auto", method = RequestMethod.GET)
    public Formation auto(Model model) {
        return formationService.createFormation(clubService.load().getTeam().getPlayers());
    }
   
}
