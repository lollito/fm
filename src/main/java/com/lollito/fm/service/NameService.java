package com.lollito.fm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.lollito.fm.utils.NameGenerator;
import com.lollito.fm.utils.RandomUtils;

@Service
public class NameService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public String generateClubName(){
		String name = "";
		List<String> prefix = Arrays.asList("A.S", "F.C", "S.S.", "A.C.");
		try {
			NameGenerator nmg = new NameGenerator("names/custom.txt");
			name = RandomUtils.randomValueFromList(prefix) + nmg.compose(RandomUtils.randomValue(3, 6));
		} catch (IOException e) {
			logger.error("ERROR {}", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		return name;
	}
	
	public List<String> getNames(){
		return getStrings("names/names.txt");
	}
	
	public List<String> getSurnames(){
		return getStrings("names/surnames.txt");
	}
	
	private List<String> getStrings(String path){
		List<String> ret = new ArrayList<>();
		try {
			ret = FileUtils.readLines(new ClassPathResource(path).getFile(), "UTF-8");
		} catch (IOException e) {
			logger.error("ERROR {}", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		return ret;
	}
}
