package com.lollito.fm.repository.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.MatchPhase;

@Repository
public interface LiveMatchSessionRepository extends JpaRepository<LiveMatchSession, Long> {
    Optional<LiveMatchSession> findByMatchId(Long matchId);
    List<LiveMatchSession> findByCurrentPhaseNot(MatchPhase phase);
}
