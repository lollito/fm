package com.lollito.fm.repository.rest;

import com.lollito.fm.model.LiveMatchSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveMatchSessionRepository extends JpaRepository<LiveMatchSession, Long> {
    Optional<LiveMatchSession> findByMatchId(Long matchId);
    List<LiveMatchSession> findByFinishedFalse();
}
