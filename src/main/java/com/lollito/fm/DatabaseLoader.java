package com.lollito.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.lollito.fm.service.CountryService;
import com.lollito.fm.service.ModuleService;
import com.lollito.fm.service.UserService;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	
	@Autowired private ModuleService moduleService;
	@Autowired private UserService userService;
	@Autowired private CountryService countryService;
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("Loading Database");
		moduleService.createModules();
		userService.create();
		countryService.create();
		logger.info("Database Loaded");
	}

}
