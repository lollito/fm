package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Club;
import com.lollito.fm.service.ClubService;

@RestController
@RequestMapping(value="/club")
public class ClubController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ClubService clubService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public Club club() {
        return clubService.load();
    }
   
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return clubService.getCount();
    }
}
