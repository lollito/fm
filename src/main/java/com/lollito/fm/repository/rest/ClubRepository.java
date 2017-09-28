package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Game;
import com.lollito.fm.model.User;

@RepositoryRestResource(collectionResourceRel = "club", path = "club")
public interface ClubRepository extends PagingAndSortingRepository<Club, Long> {
	public Club findByGameAndUser(Game game, User user);
}