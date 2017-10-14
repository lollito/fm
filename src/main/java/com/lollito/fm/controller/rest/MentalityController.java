package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Mentality;
import com.lollito.fm.model.Module;
import com.lollito.fm.service.MentalityService;
import com.lollito.fm.service.ModuleService;

@RestController
@RequestMapping(value="/mentality")
public class MentalityController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public Mentality[] module(Model model) {
        return Mentality.values();
    }
   
}
