package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.LiveMatchSession;
import com.lollito.fm.model.MatchEvent;
import com.lollito.fm.model.dto.LiveMatchSessionDTO;
import com.lollito.fm.model.dto.MatchEventDTO;
import com.lollito.fm.service.LiveMatchService;

@RestController
@RequestMapping("/api/live-match")
public class LiveMatchController {

    @Autowired
    private LiveMatchService liveMatchService;

    @PostMapping("/{matchId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveMatchSessionDTO> startLiveMatch(@PathVariable Long matchId) {
        LiveMatchSession session = liveMatchService.startLiveMatch(matchId);
        return ResponseEntity.ok(convertToDTO(session));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<LiveMatchSessionDTO> getLiveMatch(@PathVariable Long matchId) {
        LiveMatchSession session = liveMatchService.getLiveMatchSession(matchId);
        return ResponseEntity.ok(convertToDTO(session));
    }

    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEventDTO>> getMatchEvents(
            @PathVariable Long matchId,
            @RequestParam(required = false) Integer fromMinute) {
        List<MatchEvent> events = liveMatchService.getMatchEvents(matchId, fromMinute);
        return ResponseEntity.ok(events.stream()
            .map(liveMatchService::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/{matchId}/join")
    public ResponseEntity<Void> joinLiveMatch(
            @PathVariable Long matchId,
            Authentication authentication) {
        if (authentication == null) {
            // Handle unauthenticated user if needed, or rely on security config
             return ResponseEntity.status(401).build();
        }
        String userId = authentication.getName();
        liveMatchService.joinLiveMatch(matchId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{matchId}/leave")
    public ResponseEntity<Void> leaveLiveMatch(@PathVariable Long matchId) {
        liveMatchService.leaveLiveMatch(matchId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<LiveMatchSessionDTO>> getActiveLiveMatches() {
        List<LiveMatchSession> sessions = liveMatchService.getActiveLiveMatches();
        return ResponseEntity.ok(sessions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    private LiveMatchSessionDTO convertToDTO(LiveMatchSession session) {
        List<MatchEventDTO> eventDTOs = session.getEvents().stream()
                .map(liveMatchService::convertToDTO)
                .collect(Collectors.toList());

        return LiveMatchSessionDTO.builder()
                .id(session.getId())
                .match(session.getMatch())
                .currentPhase(session.getCurrentPhase())
                .currentMinute(session.getCurrentMinute())
                .additionalTime(session.getAdditionalTime())
                .matchStartTime(session.getMatchStartTime())
                .halfTimeStart(session.getHalfTimeStart())
                .secondHalfStart(session.getSecondHalfStart())
                .matchEndTime(session.getMatchEndTime())
                .isPaused(session.getIsPaused())
                .pauseReason(session.getPauseReason())
                .homeScore(session.getHomeScore())
                .awayScore(session.getAwayScore())
                .events(eventDTOs)
                .spectatorCount(session.getSpectatorCount())
                .weatherConditions(session.getWeatherConditions())
                .temperature(session.getTemperature())
                .intensity(session.getIntensity())
                .build();
    }
}
