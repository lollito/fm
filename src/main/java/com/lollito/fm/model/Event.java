package com.lollito.fm.model;

public enum Event {


	HAVE_BALL("%s have Ball"),
	HAVE_SCORED("%s have scored!"),
	HAVE_SCORED_FREE_KICK("%s have scored on free kick!"),
	HAVE_CORNER("%s have a corner"),
	COMMITS_FAUL("%s commits a faul"),
	YELLOW_CARD("%s received a yellow card,"),
	RED_CARD("%s received a red card,"),
	SHOT_AND_MISSED("%s shot and missed"),
	SUBSTITUTION("%s replaces %s");
	
	
	private String message;
	
	Event(String message){
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	
}
