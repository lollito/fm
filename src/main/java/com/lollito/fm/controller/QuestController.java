package com.lollito.fm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Quest;
import com.lollito.fm.model.QuestStatus;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.QuestRepository;
import com.lollito.fm.service.QuestService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping("/api/quests")
public class QuestController {

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private QuestService questService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Quest>> getQuests(
            @RequestParam(required = false) QuestStatus status,
            Authentication authentication) {

        User user = userService.getUser(authentication.getName());
        List<Quest> quests;

        if (status != null) {
            quests = questRepository.findByUserIdAndStatus(user.getId(), status);
        } else {
            quests = questRepository.findByUserId(user.getId());
        }

        return ResponseEntity.ok(quests);
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<Void> claimQuest(@PathVariable Long id) {
        questService.claimReward(id);
        return ResponseEntity.ok().build();
    }
}
