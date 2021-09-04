package com.lollito.fm.service;

import java.util.List;
import java.util.Locale;

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
			Locale[] all = Locale.getAvailableLocales();
			String flagUrl = "";
		    for (Locale locale : all) {
		        String displayCountry = locale.getDisplayCountry(Locale.ENGLISH);
		        if(name.equalsIgnoreCase(displayCountry)) {
		        	flagUrl = "https://www.countryflags.io/" + locale.getLanguage() + "/flat/32.png";
		        	break;
		        }
		    }
			countryRepository.save(new Country(name, createLeague, flagUrl));
		});
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
