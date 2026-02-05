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
import com.lollito.fm.service.WatchlistService;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping("/club/{clubId}")
    public ResponseEntity<WatchlistDTO> getClubWatchlist(@PathVariable Long clubId) {
        WatchlistDTO watchlist = watchlistService.getClubWatchlist(clubId);
        return ResponseEntity.ok(watchlist);
    }

    @PostMapping("/club/{clubId}/player/{playerId}")
    public ResponseEntity<WatchlistEntryDTO> addPlayerToWatchlist(
            @PathVariable Long clubId,
            @PathVariable Long playerId,
            @RequestBody AddToWatchlistRequest request) {
        WatchlistEntry entry = watchlistService.addPlayerToWatchlist(clubId, playerId, request);
        // Note: entry is WatchlistEntry entity, but we return DTO.
        // WatchlistService needs to expose a way to convert or return DTO.
        // But in the snippet, addPlayerToWatchlist returns Entry entity, and controller converts it to DTO.
        // But WatchlistService.convertToEntryDTO is private.
        // I should probably return DTO from service or make converter public.
        // I'll assume getClubWatchlist returns DTO using internal converter.
        // I will re-fetch the list or add a converter method in Service to use here.
        // Ideally Service should return DTO. But let's stick to the snippet which returns Entity.
        // Wait, the snippet says: return ResponseEntity.ok(convertToDTO(entry));
        // So the controller should have convertToDTO.

        // I'll implement convertToDTO in Controller or delegate to Service.
        // Since Service already has it, I'll make it public in Service or create a separate converter.
        // I'll assume I can't change Service visibility easily right now (already written),
        // but I can add a public method to Service: convertEntryToDTO.
        // OR I can re-implement it here (duplication).
        // Let's modify Service to have public helper or return DTO.
        // But since I already wrote Service with private method, and cannot edit it easily without `replace_with_git_merge_diff`.
        // I'll assume I can modify Service to make convertToEntryDTO public?
        // Or I can just fetch the watchlist again which returns DTOs (inefficient).

        // Actually, the snippet shows `return ResponseEntity.ok(convertToDTO(entry));` inside Controller.
        // So `convertToDTO` is a method in Controller.
        // I will implement it in Controller.
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

    // Helper method to duplicate conversion logic or I should have exposed it in service.
    // Given I can call watchlistService methods, I should use them.
    // `watchlistService.convertToDTO` for Update is public in Service (I made it so? No, I made `convertToEntryDTO` private in my previous thought but maybe I should check).
    // I can modify Service to expose these if needed.
    // I see `public WatchlistUpdateDTO convertToDTO(WatchlistUpdate update)` at the end of Service in my previous write.
    // But `convertToEntryDTO` was private.
    // I'll implement `convertToDTO` here for WatchlistEntry by reusing `WatchlistEntryDTO.builder()`.
    // I need `convertToPlayerDTO` as well.

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
