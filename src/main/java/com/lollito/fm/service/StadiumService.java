package com.lollito.fm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.StadiumRepository;
import com.lollito.fm.utils.GrandstandCapacity;

@Service
public class StadiumService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired  GrandstandCapacity groundstandCapacity;
	@Autowired  StadiumRepository stadiumRepository;
	
	public Stadium findById(Long id) {
        return stadiumRepository
          .findById(id)
          .orElseThrow(() -> new RuntimeException("Stadium '" + id + "' not found"));
    }
	
	public Integer getCapacity(Stadium stadium) {
		logger.debug("groundstandCapacity {}", groundstandCapacity.getCapacity());
		return groundstandCapacity.getCapacity().get(stadium.getGrandstandNord())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandSud())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandWest())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandEst())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandNordWest())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandNordEst())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandSudWest())
				+ groundstandCapacity.getCapacity().get(stadium.getGrandstandSudEst());
	}
}
