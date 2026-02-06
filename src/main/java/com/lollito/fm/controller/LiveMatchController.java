package com.lollito.fm.controller;

import com.lollito.fm.model.dto.LiveMatchSummaryDTO;
import com.lollito.fm.service.LiveMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live-match")
public class LiveMatchController {

    @Autowired
    private LiveMatchService liveMatchService;

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
}
