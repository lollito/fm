package com.lollito.fm.repository.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.MatchStatus;
import com.lollito.fm.model.Round;
import com.lollito.fm.model.Season;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
	public List<Match> findByRoundSeasonAndDateBeforeAndFinish(Season season, LocalDateTime date, Boolean finish);

	@Query("SELECT m FROM Match m WHERE m.round.season IN :seasons AND m.date < :date AND m.finish = :finish")
	public List<Match> findByRoundSeasonInAndDateBeforeAndFinish(@Param("seasons") List<Season> seasons, @Param("date") LocalDateTime date, @Param("finish") Boolean finish);

	@Query("SELECT m FROM Match m WHERE (m.home = :club OR m.away = :club) AND m.finish = true ORDER BY m.date DESC")
	public Page<Match> findByClubAndFinishOrderByDateDesc(@Param("club") Club club, Pageable pageable);
	public List<Match> findByStatusAndDateBefore(MatchStatus status, LocalDateTime date);
	public long countByRoundAndFinish(Round round, Boolean finish);

	@Query("SELECT m FROM Match m JOIN FETCH m.home h LEFT JOIN FETCH h.stadium JOIN FETCH m.away a JOIN FETCH m.round r JOIN FETCH r.season s JOIN FETCH s.league l WHERE m.finish = false AND (m.home.id = :clubId OR m.away.id = :clubId) ORDER BY m.date ASC")
	List<Match> findUpcomingMatchesByClub(@Param("clubId") Long clubId);

	@Query("SELECT m FROM Match m JOIN FETCH m.home JOIN FETCH m.away WHERE m.round.id = :roundId ORDER BY m.date ASC")
	List<Match> findByRoundIdWithClubs(@Param("roundId") Long roundId);

	@Query("SELECT m FROM Match m " +
			"LEFT JOIN FETCH m.home h " +
			"LEFT JOIN FETCH h.team ht " +
			"LEFT JOIN FETCH ht.formation " +
			"LEFT JOIN FETCH h.stadium " +
			"LEFT JOIN FETCH h.user " +
			"LEFT JOIN FETCH m.away a " +
			"LEFT JOIN FETCH a.team at " +
			"LEFT JOIN FETCH at.formation " +
			"LEFT JOIN FETCH a.stadium " +
			"LEFT JOIN FETCH a.user " +
			"WHERE m.id = :matchId")
	Optional<Match> findByIdWithSimulationData(@Param("matchId") Long matchId);
}