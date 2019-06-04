package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Match;
import com.lollito.fm.service.MatchService;

@RestController
@RequestMapping(value="/match")
public class MatchController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private MatchService matchService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Match> all(Model model) {
        return matchService.load();
    }
	
	@RequestMapping(value = "/next", method = RequestMethod.GET)
    public List<Match> next(Model model) {
        return matchService.loadNext();
    }
	
	@RequestMapping(value = "/previous", method = RequestMethod.GET)
    public List<Match> previuos(Model model) {
        return matchService.loadPrevious();
    }
   
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return matchService.getCount();
    }
}
