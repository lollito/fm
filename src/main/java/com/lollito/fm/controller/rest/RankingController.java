package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Ranking;
import com.lollito.fm.service.RankingService;

@RestController
@RequestMapping(value="/api/ranking")
public class RankingController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private RankingService rankingService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Ranking> game(Model model) {
        return rankingService.load();
    }
   
}
