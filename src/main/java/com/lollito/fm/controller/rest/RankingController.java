package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.dto.RankingDTO;
import com.lollito.fm.mapper.RankingMapper;
import com.lollito.fm.service.RankingService;

@RestController
@RequestMapping(value="/api/ranking")
public class RankingController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private RankingService rankingService;
	@Autowired private RankingMapper rankingMapper;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<RankingDTO> game(Model model) {
        return rankingService.load().stream()
			.map(rankingMapper::toDto)
			.collect(Collectors.toList());
    }
   
}
