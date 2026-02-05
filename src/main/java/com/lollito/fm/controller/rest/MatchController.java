package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.mapper.MatchMapper;
import com.lollito.fm.service.MatchService;
import com.lollito.fm.service.SimulationMatchService;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/match")
public class MatchController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private MatchService matchService;
	@Autowired private SimulationMatchService simulationMatchService;
	@Autowired private MatchMapper matchMapper;
	
	@GetMapping(value = "/")
    public List<MatchDTO> all(Model model) {
        return matchService.load().stream()
			.map(matchMapper::toDto)
			.collect(Collectors.toList());
    }
	
	@GetMapping(value = "/history")
    public Page<MatchDTO> history(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return matchService.loadHistory(PageRequest.of(page, size)).map(matchMapper::toDto);
    }

	@GetMapping ("/{id}")
	public ResponseEntity<MatchDTO> match (@PathVariable (value = "id") Long id ) {
		return ResponseEntity.ok( matchMapper.toDto(matchService.findById( id )) );
	}
	
	@GetMapping ("/{id}/simulate")
	public ResponseEntity<MatchDTO> simulate (@PathVariable (value = "id") Long id ) {
		Match match = matchService.findById( id );
		simulationMatchService.simulate(match);
		return ResponseEntity.ok( matchMapper.toDto(match) );
	}
	
	@GetMapping(value = "/next")
    public List<MatchDTO> next(Model model) {
        return matchService.loadNext().stream()
			.map(matchMapper::toDto)
			.collect(Collectors.toList());
    }
	
	@GetMapping(value = "/previous")
    public List<MatchDTO> previuos(Model model) {
        return matchService.loadPrevious().stream()
			.map(matchMapper::toDto)
			.collect(Collectors.toList());
    }

	@GetMapping(value = "/upcoming")
	public List<MatchDTO> upcoming(Model model) {
		return matchService.loadUpcomingMatchesForClub().stream()
			.map(matchMapper::toDto)
			.collect(Collectors.toList());
	}
   
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return matchService.getCount();
    }
}
