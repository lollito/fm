package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.dto.UpdateWatchlistEntryRequest;
import com.lollito.fm.dto.WatchlistDTO;
import com.lollito.fm.dto.WatchlistEntryDTO;
import com.lollito.fm.dto.WatchlistNotificationDTO;
import com.lollito.fm.dto.WatchlistStatsDTO;
import com.lollito.fm.dto.WatchlistUpdateDTO;
import com.lollito.fm.model.WatchlistEntry;
import com.lollito.fm.model.WatchlistUpdate;
import com.lollito.fm.model.User;
import com.lollito.fm.service.UserService;
import com.lollito.fm.service.WatchlistService;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private UserService userService;

    private void checkClubAccess(Long clubId) {
        User user = userService.getLoggedUser();
        if (user.getClub() == null || !user.getClub().getId().equals(clubId)) {
            throw new IllegalArgumentException("User does not have access to this club");
        }
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<WatchlistDTO> getClubWatchlist(@PathVariable Long clubId) {
        checkClubAccess(clubId);
        WatchlistDTO watchlist = watchlistService.getClubWatchlist(clubId);
        return ResponseEntity.ok(watchlist);
    }

    @PostMapping("/club/{clubId}/player/{playerId}")
    public ResponseEntity<WatchlistEntryDTO> addPlayerToWatchlist(
            @PathVariable Long clubId,
            @PathVariable Long playerId,
            @RequestBody AddToWatchlistRequest request) {
        checkClubAccess(clubId);
        WatchlistEntry entry = watchlistService.addPlayerToWatchlist(clubId, playerId, request);
        return ResponseEntity.ok(convertToDTO(entry));
    }

    @DeleteMapping("/entry/{entryId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long entryId) {
        watchlistService.removePlayerFromWatchlist(entryId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/entry/{entryId}")
    public ResponseEntity<WatchlistEntryDTO> updateWatchlistEntry(
            @PathVariable Long entryId,
            @RequestBody UpdateWatchlistEntryRequest request) {
        WatchlistEntry entry = watchlistService.updateWatchlistEntry(entryId, request);
        return ResponseEntity.ok(convertToDTO(entry));
    }

    @GetMapping("/club/{clubId}/notifications")
    public ResponseEntity<List<WatchlistNotificationDTO>> getNotifications(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        checkClubAccess(clubId);
        List<WatchlistNotificationDTO> notifications = watchlistService
            .getWatchlistNotifications(clubId, unreadOnly);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/notification/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        watchlistService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/club/{clubId}/stats")
    public ResponseEntity<WatchlistStatsDTO> getWatchlistStats(@PathVariable Long clubId) {
        checkClubAccess(clubId);
        WatchlistStatsDTO stats = watchlistService.getWatchlistStats(clubId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/entry/{entryId}/updates")
    public ResponseEntity<List<WatchlistUpdateDTO>> getEntryUpdates(@PathVariable Long entryId) {
        List<WatchlistUpdate> updates = watchlistService.getEntryUpdates(entryId);
        return ResponseEntity.ok(updates.stream()
            .map(watchlistService::convertToDTO)
            .collect(Collectors.toList()));
    }

    private WatchlistEntryDTO convertToDTO(WatchlistEntry entry) {
        return WatchlistEntryDTO.builder()
            .id(entry.getId())
            .player(com.lollito.fm.dto.PlayerDTO.builder()
                .id(entry.getPlayer().getId())
                .name(entry.getPlayer().getName())
                .surname(entry.getPlayer().getSurname())
                .age(entry.getPlayer().getAge())
                .role(entry.getPlayer().getRole())
                .salary(entry.getPlayer().getSalary())
                .build())
            .addedDate(entry.getAddedDate())
            .notes(entry.getNotes())
            .priority(entry.getPriority())
            .category(entry.getCategory())
            .addedValue(entry.getAddedValue())
            .currentValue(entry.getCurrentValue())
            .addedRating(entry.getAddedRating())
            .currentRating(entry.getCurrentRating())
            .notifyOnPerformance(entry.getNotifyOnPerformance())
            .notifyOnTransferStatus(entry.getNotifyOnTransferStatus())
            .notifyOnInjury(entry.getNotifyOnInjury())
            .notifyOnContractExpiry(entry.getNotifyOnContractExpiry())
            .notifyOnPriceChange(entry.getNotifyOnPriceChange())
            .totalNotifications(entry.getTotalNotifications())
            .build();
    }
}
