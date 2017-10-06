package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.User;

@Repository
public interface ClubRepository extends PagingAndSortingRepository<Club, Long> {
	public Club findByGameAndUser(Game game, User user);
}