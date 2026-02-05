package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Injury;
import com.lollito.fm.model.InjuryStatus;

@Repository
public interface InjuryRepository extends JpaRepository<Injury, Long> {

    List<Injury> findByStatus(InjuryStatus status);

    List<Injury> findByPlayerId(Long playerId);

    List<Injury> findByPlayerTeamIdAndStatus(Long teamId, InjuryStatus status);
}
