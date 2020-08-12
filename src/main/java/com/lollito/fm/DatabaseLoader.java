package com.lollito.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.service.CountryService;
import com.lollito.fm.service.GameService;
import com.lollito.fm.service.ModuleService;
import com.lollito.fm.service.UserService;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	
	@Autowired private ModuleService moduleService;
	@Autowired private GameService gameService;
	@Autowired private CountryService countryService;
	@Autowired private UserService userService;
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("Loading Database");
		moduleService.createModules();
		countryService.create();
		gameService.create("Test");
		RegistrationRequest registration = new RegistrationRequest();
		registration.setEmail("ciao");
		registration.setPassword("ciao");
		registration.setPasswordConfirm("ciao");
		registration.setUsername("lollito");
		registration.setClubName("Roma");
		registration.setCountryId(countryService.findByCreateLeague(true).get(0).getId());
		userService.save(registration);
		logger.info("Database Loaded");
	}

}
