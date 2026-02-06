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
import com.lollito.fm.model.dto.ModuleDTO;
import com.lollito.fm.service.ModuleService;
import com.lollito.fm.mapper.ModuleMapper;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/module")
public class ModuleController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ModuleService moduleService;
	@Autowired private ModuleMapper moduleMapper;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<ModuleDTO> module(Model model) {
        return moduleService.findAll().stream()
			.map(moduleMapper::toDto)
			.collect(Collectors.toList());
    }
   
}
