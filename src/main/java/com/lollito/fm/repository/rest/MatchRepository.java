package com.lollito.fm.repository.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.Season;

@Repository
public interface MatchRepository extends PagingAndSortingRepository<Match, Long> {
	public List<Match> findByRoundSeasonAndDateBeforeAndFinish(Season season, LocalDateTime date, Boolean finish);
}