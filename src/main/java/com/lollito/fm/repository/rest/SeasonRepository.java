package com.lollito.fm.repository.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Season;

import jakarta.persistence.LockModeType;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Season s WHERE s.id = :id")
    Optional<Season> findByIdWithLock(@Param("id") Long id);

    Optional<Season> findByCurrentTrue();

    List<Season> findAllByCurrentTrue();
}