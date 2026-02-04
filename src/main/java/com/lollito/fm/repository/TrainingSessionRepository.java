package com.lollito.fm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Team;
import com.lollito.fm.model.TrainingSession;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    Page<TrainingSession> findByTeam(Team team, Pageable pageable);
}
