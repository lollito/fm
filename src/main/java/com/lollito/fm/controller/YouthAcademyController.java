package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.mapper.PlayerMapper;
import com.lollito.fm.mapper.YouthCandidateMapper;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.dto.YouthCandidateDTO;
import com.lollito.fm.service.YouthAcademyService;

@RestController
@RequestMapping("/api/youth-academy")
public class YouthAcademyController {

    @Autowired private YouthAcademyService youthAcademyService;
    @Autowired private YouthCandidateMapper youthCandidateMapper;
    @Autowired private PlayerMapper playerMapper;

    @GetMapping("/{id}/candidates")
    public ResponseEntity<List<YouthCandidateDTO>> getCandidates(@PathVariable Long id) {
        return ResponseEntity.ok(
            youthAcademyService.getCandidates(id).stream()
                .map(youthCandidateMapper::toDto)
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/promote/{candidateId}")
    public ResponseEntity<PlayerDTO> promoteCandidate(@PathVariable Long candidateId) {
        Player player = youthAcademyService.promoteCandidate(candidateId);
        return ResponseEntity.ok(playerMapper.toDto(player));
    }
}
