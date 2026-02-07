package com.lollito.fm.repository.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.LiveMatchSession;

@Repository
public interface LiveMatchSessionRepository extends MongoRepository<LiveMatchSession, String> {
    Optional<LiveMatchSession> findByMatchId(Long matchId);
    List<LiveMatchSession> findByFinishedFalse();
}
