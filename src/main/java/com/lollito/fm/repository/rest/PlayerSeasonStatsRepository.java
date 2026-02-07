package com.lollito.fm.repository.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerSeasonStats;
import com.lollito.fm.model.Season;

@Repository
public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {

    Optional<PlayerSeasonStats> findByPlayerAndSeason(Player player, Season season);

    List<PlayerSeasonStats> findBySeasonAndPlayerIn(Season season, List<Player> players);

    List<PlayerSeasonStats> findByPlayer(Player player);

    List<PlayerSeasonStats> findByPlayerOrderBySeasonDesc(Player player);

    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.league.id = :leagueId AND s.season = :season ORDER BY s.goals DESC")
    List<PlayerSeasonStats> findTopScorersByLeagueAndSeason(@Param("leagueId") Long leagueId, @Param("season") Season season, Pageable pageable);

    @Query("SELECT s FROM PlayerSeasonStats s WHERE s.league.id = :leagueId AND s.season = :season ORDER BY s.assists DESC")
    List<PlayerSeasonStats> findTopAssistsByLeagueAndSeason(@Param("leagueId") Long leagueId, @Param("season") Season season, Pageable pageable);
}
