package com.lollito.fm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.StadiumRepository;

@Service
public class StadiumService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired  StadiumRepository stadiumRepository;
	
	public Stadium findById(Long id) {
        return stadiumRepository
          .findById(id)
          .orElseThrow(() -> new RuntimeException("Stadium '" + id + "' not found"));
    }
	
	public Integer getCapacity(Stadium stadium) {
		return stadium.getCapacity();
	}
}
