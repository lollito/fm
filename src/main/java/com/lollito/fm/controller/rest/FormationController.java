package com.lollito.fm.controller.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.rest.FormationRequest;
import com.lollito.fm.service.FormationService;

@RestController
@RequestMapping(value="/formation")
public class FormationController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private FormationService formationService;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
    public Formation create(FormationRequest formationRequest) {
		return formationService.createPlayerFormation(formationRequest);
    }

	@RequestMapping(value = "/auto", method = RequestMethod.GET)
    public Formation auto(Model model) {
		return formationService.createPlayerFormation();
    }

   
}
