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
import com.lollito.fm.service.MatchService;
import com.lollito.fm.service.SimulationMatchService;

@RestController
@RequestMapping(value="/api/match")
public class MatchController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private MatchService matchService;
	@Autowired private SimulationMatchService simulationMatchService;
	
	@GetMapping(value = "/")
    public List<Match> all(Model model) {
        return matchService.load();
    }
	
	@GetMapping(value = "/history")
    public Page<Match> history(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return matchService.loadHistory(PageRequest.of(page, size));
    }

	@GetMapping ("/{id}")
	public ResponseEntity<Match> match (@PathVariable (value = "id") Long id ) {
		return ResponseEntity.ok( matchService.findById( id ) );
	}
	
	@GetMapping ("/{id}/simulate")
	public ResponseEntity<Match> simulate (@PathVariable (value = "id") Long id ) {
		Match match = matchService.findById( id );
		simulationMatchService.simulate(match);
		return ResponseEntity.ok( match );
	}
	
	@GetMapping(value = "/next")
    public List<Match> next(Model model) {
        return matchService.loadNext();
    }
	
	@GetMapping(value = "/previous")
    public List<Match> previuos(Model model) {
        return matchService.loadPrevious();
    }

	@GetMapping(value = "/upcoming")
	public List<Match> upcoming(Model model) {
		return matchService.loadUpcomingMatchesForClub();
	}
   
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return matchService.getCount();
    }
}
