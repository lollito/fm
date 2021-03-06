package com.lollito.fm.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.lollito.fm.utils.NameGenerator;
import com.lollito.fm.utils.RandomUtils;

@Service
public class NameService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired NameGenerator nameGenerator;
	
	public String generateClubName(){
		List<String> prefix = Arrays.asList("A.S. ", "F.C. ", "S.S. ", "A.C. ", "", "", "", "", "");
		
		return RandomUtils.randomValueFromList(prefix) + nameGenerator.compose(RandomUtils.randomValue(2, 5));
		
	}
	
	public List<String> getNames(){
		return getStrings("/name/name.txt");
	}
	
	public List<String> getSurnames(){
		return getStrings("/name/surname.txt");
	}
	
	public List<String> getCountryFileLanes(){
		return getStrings("/name/country.txt");
	}
	
	private List<String> getStrings(String path){
		List<String> ret = new ArrayList<>();
		BufferedReader bufRead;
        String line;

        try {
			bufRead = new BufferedReader(new InputStreamReader(new ClassPathResource(path).getInputStream(), "UTF-8"));
			line = "";
	        while (line != null) {
	            line = bufRead.readLine();
	            if(line != null && !line.startsWith("#")) {
	            	ret.add(line);
	            }
	        }
	        bufRead.close();
		} catch (IOException e) {
			logger.error("ERROR {}", e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
        
        
		return ret;
	}
}
