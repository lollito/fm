package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Country;
import com.lollito.fm.repository.rest.CountryRepository;

@Service
public class CountryService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired NameService nameService;
	@Autowired CountryRepository countryRepository;
	
	public void create(){
		nameService.getCountryFileLanes().parallelStream().forEach(lane -> {
			String[] tokens = lane.split(",");
			String name = tokens[0];
			Boolean createLeague = Boolean.valueOf(tokens[1]);
			countryRepository.save(new Country(name, createLeague));
		});;
	}
	
	public Long getCount() {
		return countryRepository.count();
	}
	
	public List<Country> findAll() {
		return countryRepository.findAll();
	}
	
	public List<Country> findByCreateLeague(Boolean createLeague) {
		return countryRepository.findByCreateLeague(createLeague);
	}
	
}
