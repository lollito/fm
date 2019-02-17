package com.lollito.fm.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.Mentality;
import com.lollito.fm.utils.RandomUtils;

@Service
public class MentalityService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public Mentality random(){
		return RandomUtils.randomValueFromList(Arrays.asList(Mentality.values()));
	}
}
