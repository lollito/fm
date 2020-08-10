package com.lollito.fm.model;

public enum Event {


	HAVE_BALL("%s have Ball"),
	HAVE_SCORED("%s have Scored!");
	
	
	private String message;
	
	Event(String message){
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	
}
