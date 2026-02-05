package com.lollito.fm;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.repository.rest.UserRepository;
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
	@Autowired private UserRepository userRepository;
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("Loading Database");

		// Setup System User for Game Creation
		if (!userRepository.existsByUsername("system")) {
			User systemUser = new User();
			systemUser.setUsername("system");
			systemUser.setPassword("syspass");
			systemUser.setEmail("sys@sys.com");
			systemUser.setActive(true);
			userRepository.save(systemUser);
		}

		// Auth as system to create game (which creates clubs)
		UserDetails sysDetails = new org.springframework.security.core.userdetails.User("system", "syspass", Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(sysDetails, null, sysDetails.getAuthorities())
		);

		if (moduleService.findAll().isEmpty()) {
			moduleService.createModules();
		}
		if (countryService.getCount() == 0) {
			countryService.create();
		}

		gameService.create("SystemGame");

		// Create default user
		if (!userService.existsByUsername("lollito")) {
			RegistrationRequest registration = new RegistrationRequest();
			registration.setEmail("ciao");
			registration.setPassword("ciao");
			registration.setPasswordConfirm("ciao");
			registration.setUsername("lollito");
			registration.setClubName("Roma");
			registration.setCountryId(countryService.findByCreateLeague(true).get(0).getId());
			userService.save(registration);
		}

		logger.info("Database Loaded");
	}

}
