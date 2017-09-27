package com.lollito.fm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.lollito.fm.service.ModuleService;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	
	@Autowired private ModuleService moduleService;
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("start run");
		moduleService.createModules();
	}

}
