package com.lollito.fm.utils;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "grandstand")
public class GrandstandCapacity {
	
	private Map<Integer, Integer> capacity;

	public Map<Integer, Integer> getCapacity() {
		return capacity;
	}
	
	public void setCapacity(Map<Integer, Integer> capacity) {
		this.capacity = capacity;
	}
	
	
}
