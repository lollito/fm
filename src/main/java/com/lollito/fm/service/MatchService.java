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
	
	public List<Match> loadNext(){
		return loadByRoundNumber(sessionBean.getGame().getCurrentSeason().getNextRoundNumber() - 1);
	}
	
	public List<Match> loadPrevious(){
		Integer nextRoundNumber = sessionBean.getGame().getCurrentSeason().getNextRoundNumber();
		return loadByRoundNumber(nextRoundNumber > 1 ? nextRoundNumber - 2 : nextRoundNumber - 1);
	}
	
	private List<Match> loadByRoundNumber(Integer number){
		if(sessionBean.getGame().getCurrentSeason().getRounds().size() <= number) {
			return new ArrayList<>();
		}
		return sessionBean.getGame().getCurrentSeason().getRounds().get(number).getMatches();
	}
}
