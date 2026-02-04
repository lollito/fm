package com.lollito.fm.repository.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.DebugAction;
import com.lollito.fm.model.DebugActionStatus;
import com.lollito.fm.model.DebugActionType;

@Repository
public interface DebugActionRepository extends JpaRepository<DebugAction, Long> {
    Long countByExecutedAtAfter(LocalDateTime date);

    Page<DebugAction> findByActionTypeAndStatus(DebugActionType actionType, DebugActionStatus status, Pageable pageable);
    Page<DebugAction> findByActionType(DebugActionType actionType, Pageable pageable);
    Page<DebugAction> findByStatus(DebugActionStatus status, Pageable pageable);
}
