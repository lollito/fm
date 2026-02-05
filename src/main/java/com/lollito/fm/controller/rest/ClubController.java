package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.dto.ClubDTO;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.service.ClubService;
import com.lollito.fm.service.UserService;
import com.lollito.fm.mapper.ClubMapper;

@RestController
@RequestMapping(value="/api/club")
public class ClubController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private ClubService clubService;
	@Autowired private UserService userService;
	@Autowired private CountryRepository countryRepository;
	@Autowired private ClubMapper clubMapper;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public ClubDTO club() {
        return clubMapper.toDto(userService.getLoggedUser().getClub());
    }
   
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return clubService.getCount();
    }
	
	@RequestMapping(value = "/findTopByLeagueCountry/{countryId}", method = RequestMethod.GET)
    public ClubDTO findTopByLeagueCountry(@PathVariable (value = "countryId") Long countryId) {
        return clubMapper.toDto(clubService.findTopByLeagueCountry(countryRepository.findById(countryId).get()));
    }
	
	@RequestMapping(value = "/findTopByLeagueCountryAndUserIsNull/{countryId}", method = RequestMethod.GET)
    public ClubDTO findTopByLeagueCountryAndUserIsNull(@PathVariable (value = "countryId") Long countryId) {
        return clubMapper.toDto(clubService.findTopByLeagueCountryAndUserIsNull(countryRepository.findById(countryId).get()));
    }
}
