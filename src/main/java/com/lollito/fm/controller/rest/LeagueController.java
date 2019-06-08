package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.service.LeagueService;

@RestController
@RequestMapping(value="/league")
public class LeagueController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private LeagueService leagueService;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long game(Model model) {
        return leagueService.getCount();
    }
   
}
