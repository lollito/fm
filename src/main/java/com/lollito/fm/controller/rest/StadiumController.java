package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Stadium;
import com.lollito.fm.service.StadiumService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping(value="/api/stadium")
public class StadiumController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private StadiumService stadiumService;
	@Autowired private UserService userService;
	
	@GetMapping(value = "/")
    public Stadium stadium() {
        return userService.getLoggedUser().getClub().getStadium();
    }
	
	@GetMapping(value = "/{id}")
    public Stadium findById(@PathVariable(value="id") Long id) {
        return stadiumService.findById(id);
    }
   
}
