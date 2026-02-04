package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.SystemSnapshot;

@Repository
public interface SystemSnapshotRepository extends JpaRepository<SystemSnapshot, Long> {
    List<SystemSnapshot> findByOrderByCreatedAtDesc(Pageable pageable);
}
