package com.lollito.fm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerScoutingStatus;

@Repository
public interface PlayerScoutingStatusRepository extends JpaRepository<PlayerScoutingStatus, Long> {
    Optional<PlayerScoutingStatus> findByPlayerAndScoutingClub(Player player, Club scoutingClub);
}
