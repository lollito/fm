package com.lollito.fm.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.lollito.fm.controller.rest.errors.GameNotFoundException;
import com.lollito.fm.model.Game;
import com.lollito.fm.repository.rest.GameRepository;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionBean {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	GameRepository gameRepository;

	private Long gameId;

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public Game getGame() {
		if (this.gameId == null) {
			throw new GameNotFoundException("Game ID is not set in session");
		}
		return gameRepository.findById(this.gameId)
				.orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + this.gameId));
	}
}
