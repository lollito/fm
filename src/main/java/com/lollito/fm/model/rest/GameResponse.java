package com.lollito.fm.model.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.lollito.fm.model.Match;

public class GameResponse {

	private LocalDate currentDate;
	private Match currentMatch;
	private List<Match> disputatedMatch = new ArrayList<>();
	
	public GameResponse() {
	}
	
	public GameResponse(LocalDate currentDate) {
		this.currentDate = currentDate;
	}

	public LocalDate getCurrentDate() {
		return currentDate;
	}
	
	public void setCurrentDate(LocalDate currentDate) {
		this.currentDate = currentDate;
	}
	
	public Match getCurrentMatch() {
		return currentMatch;
	}
	
	public void setCurrentMatch(Match currentMatch) {
		this.currentMatch = currentMatch;
	}
	
	public List<Match> getDisputatedMatch() {
		return disputatedMatch;
	}
	
	public void setDisputatedMatch(List<Match> disputatedMatch) {
		this.disputatedMatch = disputatedMatch;
	}
	
}
