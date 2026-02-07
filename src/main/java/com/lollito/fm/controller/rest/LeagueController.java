package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.mapper.LeagueMapper;
import com.lollito.fm.model.dto.LeagueDTO;
import com.lollito.fm.service.LeagueService;

@RestController
@RequestMapping(value="/api/league")
public class LeagueController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private LeagueService leagueService;
	@Autowired private LeagueMapper leagueMapper;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long game(Model model) {
        return leagueService.getCount();
    }
	
	@GetMapping(value = "/")
    public List<LeagueDTO> findAll(Model model) {
        return leagueService.findAll().stream()
			.map(leagueMapper::toDto)
			.collect(Collectors.toList());
    }
   
}
