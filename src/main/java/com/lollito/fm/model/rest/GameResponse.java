package com.lollito.fm.model.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lollito.fm.model.dto.MatchDTO;

public class GameResponse {

	private LocalDateTime currentDate;
	private List<MatchDTO> disputatedMatch = new ArrayList<>();
	
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
	
	public List<MatchDTO> getDisputatedMatch() {
		return disputatedMatch;
	}
	
	public void setDisputatedMatch(List<MatchDTO> disputatedMatch) {
		this.disputatedMatch = disputatedMatch;
	}
	
}
