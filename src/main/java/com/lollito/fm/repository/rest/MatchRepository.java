package com.lollito.fm.repository.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
	public List<Match> findByRoundSeasonAndDateBeforeAndFinish(Season season, LocalDateTime date, Boolean finish);
	public List<Match> findByStatusAndDateBefore(MatchStatus status, LocalDateTime date);
	public long countByRoundAndFinish(Round round, Boolean finish);

	@Query("SELECT m FROM Match m JOIN FETCH m.home h LEFT JOIN FETCH h.stadium JOIN FETCH m.away a JOIN FETCH m.round r JOIN FETCH r.season s JOIN FETCH s.league l WHERE m.finish = false AND (m.home.id = :clubId OR m.away.id = :clubId) ORDER BY m.date ASC")
	List<Match> findUpcomingMatchesByClub(@Param("clubId") Long clubId);
}