package com.lollito.fm.config.security;

import java.io.Serializable;

public class UserObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8526593055313405121L;
	private String username;
	private String userId;

	
	public UserObject(String username, String userId) {
		this.username = username;
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "UserObject [username=" + username + ", userId=" + userId + "]";
	}
	
}
