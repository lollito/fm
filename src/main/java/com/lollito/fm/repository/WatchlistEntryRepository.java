package com.lollito.fm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.Watchlist;
import com.lollito.fm.model.WatchlistEntry;

@Repository
public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry, Long> {
    Optional<WatchlistEntry> findByWatchlistAndPlayer(Watchlist watchlist, Player player);

    @Query("SELECT e FROM WatchlistEntry e WHERE e.watchlist.isActive = true")
    List<WatchlistEntry> findAllActive();

    List<WatchlistEntry> findByWatchlist(Watchlist watchlist);
}
