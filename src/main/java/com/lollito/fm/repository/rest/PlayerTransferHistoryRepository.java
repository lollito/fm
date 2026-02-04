package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTransferHistory;

@Repository
public interface PlayerTransferHistoryRepository extends JpaRepository<PlayerTransferHistory, Long> {

    List<PlayerTransferHistory> findByPlayerOrderByTransferDateDesc(Player player);
}
