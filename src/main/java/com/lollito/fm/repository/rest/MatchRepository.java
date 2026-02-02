package com.lollito.fm.repository.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Season;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
	public List<Match> findByRoundSeasonAndDateBeforeAndFinish(Season season, LocalDateTime date, Boolean finish);

	@Query("SELECT m FROM Match m WHERE (m.home = :club OR m.away = :club) AND m.finish = true ORDER BY m.date DESC")
	public Page<Match> findByClubAndFinishOrderByDateDesc(@Param("club") Club club, Pageable pageable);
}