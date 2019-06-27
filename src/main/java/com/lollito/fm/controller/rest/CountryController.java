package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.service.CountryService;

@RestController
@RequestMapping(value="/country")
public class CountryController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private CountryService countryService;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long game(Model model) {
        return countryService.getCount();
    }
   
}
