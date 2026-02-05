package com.lollito.fm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.MatchPlayerStats;

@Repository
public interface MatchPlayerStatsRepository extends JpaRepository<MatchPlayerStats, Long> {

    @Query("SELECT mps FROM MatchPlayerStats mps JOIN mps.match m WHERE mps.player.id = :playerId AND m.date >= :startDate")
    List<MatchPlayerStats> findRecentStats(@Param("playerId") Long playerId, @Param("startDate") LocalDateTime startDate);
}
