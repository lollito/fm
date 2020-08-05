package com.lollito.fm.model;

public enum Event {


	HAVE_BALL				("Have Ball");
	
	
	private String message;
	
	Event(String message){
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	
}
