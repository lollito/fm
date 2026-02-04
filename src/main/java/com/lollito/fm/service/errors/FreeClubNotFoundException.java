package com.lollito.fm.service.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Free club not found")
public class FreeClubNotFoundException extends RuntimeException {

}
