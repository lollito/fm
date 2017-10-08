package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Module;
import com.lollito.fm.service.ModuleService;

@RestController
@RequestMapping(value="/module")
public class ModuleController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ModuleService moduleService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Module> game(Model model) {
        return moduleService.findAll();
    }
   
}
