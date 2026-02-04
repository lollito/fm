package com.lollito.fm.controller.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GameNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GameNotFoundException(String message) {
	private static final long serialVersionUID = 1L;

	public GameNotFoundException(String message) {
        super(message);
    }
}
