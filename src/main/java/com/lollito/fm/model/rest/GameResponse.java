package com.lollito.fm.model.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lollito.fm.model.Match;

public class GameResponse {

	private LocalDateTime currentDate;
	private List<Match> disputatedMatch = new ArrayList<>();
	
	public GameResponse() {
	}
	
	public GameResponse(LocalDateTime currentDate) {
		this.currentDate = currentDate;
	}

	public LocalDateTime getCurrentDate() {
		return currentDate;
	}
	
	public void setCurrentDate(LocalDateTime currentDate) {
		this.currentDate = currentDate;
	}
	
	public List<Match> getDisputatedMatch() {
		return disputatedMatch;
	}
	
	public void setDisputatedMatch(List<Match> disputatedMatch) {
		this.disputatedMatch = disputatedMatch;
	}
	
}
