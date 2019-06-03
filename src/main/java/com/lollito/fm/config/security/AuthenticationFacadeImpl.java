package com.lollito.fm.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

	@Override
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	@Override
	public UserObject getAuthenticationDetails() {
		Object object = SecurityContextHolder.getContext().getAuthentication().getDetails();
		return (UserObject) object;
	}
}