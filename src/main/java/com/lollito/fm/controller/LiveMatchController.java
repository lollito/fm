package com.lollito.fm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.lollito.fm.model.dto.LiveMatchSummaryDTO;
import com.lollito.fm.service.LiveMatchService;

@RestController
@RequestMapping("/api/live-match")
public class LiveMatchController {

    @Autowired
    private LiveMatchService liveMatchService;

    @GetMapping("/current")
    public ResponseEntity<List<LiveMatchService.LiveMatchData>> getCurrentLiveMatches() {
        return ResponseEntity.ok(liveMatchService.getAllLiveMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getLiveMatch(@PathVariable Long id) {
        return ResponseEntity.ok(liveMatchService.getLiveMatchData(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<LiveMatchSummaryDTO>> getAllLiveMatches() {
        return ResponseEntity.ok(liveMatchService.getAllLiveMatchSummaries());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinLiveMatch(@PathVariable Long id) {
        // Logic for tracking spectators could go here
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveLiveMatch(@PathVariable Long id) {
        // Logic for tracking spectators could go here
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> finishLiveMatch(@PathVariable Long id) {
        liveMatchService.forceFinish(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetLiveMatch(@PathVariable Long id) {
        liveMatchService.reset(id);
        return ResponseEntity.ok().build();
    }
}
