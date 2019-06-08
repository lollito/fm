package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.League;
import com.lollito.fm.repository.rest.LeagueRepository;

@Service
public class LeagueService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired LeagueRepository leagueRepository;
	
	
	public Long getCount() {
		return leagueRepository.count();
	}
	
	public List<League> findAll() {
		return leagueRepository.findAll();
	}
	
	
}
