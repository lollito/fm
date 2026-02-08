package com.lollito.fm.controller.rest;

import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.ManagerProfileDTO;
import com.lollito.fm.model.dto.UnlockPerkRequest;
import com.lollito.fm.service.ManagerProgressionService;
import com.lollito.fm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerProgressionService managerProgressionService;
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ManagerProfileDTO> getProfile(Authentication authentication) {
        User user = userService.getUser(authentication.getName());
        ManagerProfile profile = managerProgressionService.getProfile(user);
        return ResponseEntity.ok(convertToDTO(profile));
    }

    @PostMapping("/unlock-perk")
    public ResponseEntity<ManagerProfileDTO> unlockPerk(@RequestBody UnlockPerkRequest request, Authentication authentication) {
        User user = userService.getUser(authentication.getName());
        managerProgressionService.unlockPerk(user, request.getPerkId());
        ManagerProfile profile = managerProgressionService.getProfile(user);
        return ResponseEntity.ok(convertToDTO(profile));
    }

    private ManagerProfileDTO convertToDTO(ManagerProfile profile) {
        long xpForNextLevel = profile.getLevel() * ManagerProgressionService.LEVEL_XP_MULTIPLIER;

        return ManagerProfileDTO.builder()
                .id(profile.getId())
                .level(profile.getLevel())
                .currentXp(profile.getCurrentXp())
                .talentPoints(profile.getTalentPoints())
                .unlockedPerks(profile.getUnlockedPerks())
                .xpForNextLevel(xpForNextLevel)
                .build();
    }
}
