package com.lollito.fm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.PlayerTrainingResult;

@Repository
public interface PlayerTrainingResultRepository extends JpaRepository<PlayerTrainingResult, Long> {
}
