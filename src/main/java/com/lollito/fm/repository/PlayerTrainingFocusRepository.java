package com.lollito.fm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTrainingFocus;

public interface PlayerTrainingFocusRepository extends JpaRepository<PlayerTrainingFocus, Long> {

    @Query("SELECT ptf FROM PlayerTrainingFocus ptf WHERE ptf.player = :player AND :date BETWEEN ptf.startDate AND ptf.endDate")
    List<PlayerTrainingFocus> findActiveFocus(@Param("player") Player player, @Param("date") LocalDate date);

    @Query("SELECT ptf FROM PlayerTrainingFocus ptf WHERE ptf.player IN :players AND :date BETWEEN ptf.startDate AND ptf.endDate")
    List<PlayerTrainingFocus> findActiveFocusForPlayers(@Param("players") List<Player> players, @Param("date") LocalDate date);

    @Query("SELECT COUNT(ptf) > 0 FROM PlayerTrainingFocus ptf WHERE ptf.player = :player AND " +
           "(ptf.startDate <= :endDate AND ptf.endDate >= :startDate)")
    boolean existsOverlappingFocus(@Param("player") Player player, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
