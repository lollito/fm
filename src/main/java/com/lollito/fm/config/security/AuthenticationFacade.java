package com.lollito.fm.config.security;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
	Authentication getAuthentication();

	UserObject getAuthenticationDetails();
}
