package com.lollito.fm.bean;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.lollito.fm.model.Game;

@Component
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionBean {

	private Long gameId;

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

}
