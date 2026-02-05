package com.lollito.fm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.WatchlistEntry;
import com.lollito.fm.model.WatchlistUpdate;

@Repository
public interface WatchlistUpdateRepository extends JpaRepository<WatchlistUpdate, Long> {
    List<WatchlistUpdate> findByWatchlistEntryOrderByUpdateDateDesc(WatchlistEntry entry);
}
