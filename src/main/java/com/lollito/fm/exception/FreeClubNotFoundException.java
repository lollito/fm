package com.lollito.fm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FreeClubNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FreeClubNotFoundException() {
        super("free club not found");
    }

    public FreeClubNotFoundException(String message) {
        super(message);
    }
}
