package com.lollito.fm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Watchlist;
import com.lollito.fm.model.WatchlistNotification;

@Repository
public interface WatchlistNotificationRepository extends JpaRepository<WatchlistNotification, Long> {

    @Query("SELECT n FROM WatchlistNotification n WHERE n.watchlistEntry.watchlist = :watchlist ORDER BY n.createdDate DESC")
    List<WatchlistNotification> findByWatchlistOrderByCreatedDateDesc(@Param("watchlist") Watchlist watchlist);

    @Query("SELECT n FROM WatchlistNotification n WHERE n.watchlistEntry.watchlist = :watchlist AND (:unreadOnly = false OR n.isRead = false) ORDER BY n.createdDate DESC")
    List<WatchlistNotification> findByWatchlistOrderByCreatedDateDesc(@Param("watchlist") Watchlist watchlist, @Param("unreadOnly") Boolean unreadOnly);
}
