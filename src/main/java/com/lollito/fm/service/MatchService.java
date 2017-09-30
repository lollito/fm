package com.lollito.fm.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.bean.SessionBean;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Round;

@Service
public class MatchService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired SessionBean sessionBean;
	
	public List<Match> load(){
		List<Match> matches = new ArrayList<>();
		for(Round round : sessionBean.getGame().getCurrentSeason().getRounds()){
			matches.addAll(round.getMatches());
		}
		return matches;
	}
}
