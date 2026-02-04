package com.lollito.fm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutStatus;

@Repository
public interface ScoutRepository extends JpaRepository<Scout, Long> {
    List<Scout> findByClub(Club club);
    List<Scout> findByClubAndStatus(Club club, ScoutStatus status);
}
