package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Injury;
import com.lollito.fm.model.dto.CreateInjuryRequest;
import com.lollito.fm.model.dto.InjuryDTO;
import com.lollito.fm.service.InjuryService;

@RestController
@RequestMapping("/api/injuries")
public class InjuryController {

    @Autowired
    private InjuryService injuryService;

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<InjuryDTO>> getTeamInjuries(
            @PathVariable Long teamId) {
        List<Injury> injuries = injuryService.getTeamInjuries(teamId);
        return ResponseEntity.ok(injuries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<InjuryDTO>> getClubInjuries(
            @PathVariable Long clubId) {
        List<Injury> injuries = injuryService.getClubInjuries(clubId);
        return ResponseEntity.ok(injuries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/player/{playerId}/history")
    public ResponseEntity<List<InjuryDTO>> getPlayerInjuryHistory(
            @PathVariable Long playerId) {
        List<Injury> injuries = injuryService.getPlayerInjuryHistory(playerId);
        return ResponseEntity.ok(injuries.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/player/{playerId}/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InjuryDTO> createManualInjury(
            @PathVariable Long playerId,
            @RequestBody CreateInjuryRequest request) {
        Injury injury = injuryService.createManualInjury(playerId, request);
        return ResponseEntity.ok(convertToDTO(injury));
    }

    private InjuryDTO convertToDTO(Injury injury) {
        InjuryDTO dto = new InjuryDTO();
        dto.setId(injury.getId());
        dto.setPlayerId(injury.getPlayer().getId());
        dto.setPlayerName(injury.getPlayer().getName());
        dto.setPlayerSurname(injury.getPlayer().getSurname());
        dto.setType(injury.getType());
        dto.setSeverity(injury.getSeverity());
        dto.setInjuryDate(injury.getInjuryDate());
        dto.setExpectedRecoveryDate(injury.getExpectedRecoveryDate());
        dto.setActualRecoveryDate(injury.getActualRecoveryDate());
        dto.setStatus(injury.getStatus());
        dto.setPerformanceImpact(injury.getPerformanceImpact());
        dto.setDescription(injury.getDescription());
        return dto;
    }
}
