package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.MatchEvent;

@Repository
public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findByMatchId(Long matchId);
    List<MatchEvent> findByMatchIdAndMinuteGreaterThanEqual(Long matchId, Integer minute);
}
