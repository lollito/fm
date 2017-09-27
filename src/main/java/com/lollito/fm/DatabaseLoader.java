package com.lollito.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.lollito.fm.service.ModuleService;
import com.lollito.fm.service.UserService;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	
	@Autowired private ModuleService moduleService;
	@Autowired private UserService userService;
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("loading database");
		moduleService.createModules();
		userService.create();
		logger.info("database loaded");
	}

}
