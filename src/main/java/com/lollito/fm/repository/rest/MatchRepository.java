package com.lollito.fm.repository.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Game;
import com.lollito.fm.model.Match;

@RepositoryRestResource(collectionResourceRel = "match", path = "match")
public interface MatchRepository extends PagingAndSortingRepository<Match, Long> {
	public List<Match> findByGameAndDate(Game game, LocalDate date);
}