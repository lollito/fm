package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Module;
import com.lollito.fm.repository.rest.ModuleRepository;
import com.lollito.fm.utils.RandomUtils;

@Service
public class ModuleService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired ModuleRepository moduleRepository;
	
	public List<Module> createModules() {
		List<Module> modules = new ArrayList<>();
		modules.add(new Module("4-4-2", 2, 2, 2, 2, 2));
		modules.add(new Module("4-3-3", 2, 2, 3, 2, 1));
		modules.add(new Module("4-3-3 offensive", 2, 2, 3, 0, 3));
		moduleRepository.save(modules);
		return modules;
	}
	
	public Module findOne(Long id) {
		return moduleRepository.findOne(id);
	}
	
	
	public List<Module> findAll() {
		return moduleRepository.findAll();
	}
	
	public Module randomModule(){
		return RandomUtils.randomValueFromList(moduleRepository.findAll());
	}
}
