package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.SessionStatus;
import com.lollito.fm.model.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    long countByStatusAndIsActive(SessionStatus status, Boolean isActive);
    List<UserSession> findByUserIdOrderByLoginTimeDesc(Long userId);
}
