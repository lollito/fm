package com.lollito.fm.service;

import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.ManagerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerProgressionService {

    private final ManagerProfileRepository managerProfileRepository;
    private final NotificationService notificationService;

    public static final long XP_WIN = 100;
    public static final long XP_DRAW = 50;
    public static final long XP_LOSS = 10;
    public static final long LEVEL_XP_MULTIPLIER = 1000;

    @Transactional
    public ManagerProfile getProfile(User user) {
        return managerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> createProfile(user));
    }

    private ManagerProfile createProfile(User user) {
        ManagerProfile profile = ManagerProfile.builder()
                .user(user)
                .level(1)
                .currentXp(0L)
                .talentPoints(0)
                .unlockedPerks(new HashSet<>())
                .build();
        return managerProfileRepository.save(profile);
    }

    @Transactional
    public void addXp(User user, long amount) {
        if (user == null || amount <= 0) {
            return;
        }

        ManagerProfile profile = getProfile(user);

        long currentXp = profile.getCurrentXp() != null ? profile.getCurrentXp() : 0L;
        profile.setCurrentXp(currentXp + amount);

        checkLevelUp(profile);
        managerProfileRepository.save(profile);
    }

    private void checkLevelUp(ManagerProfile profile) {
        int level = profile.getLevel() != null ? profile.getLevel() : 1;
        long xpNeeded = xpForNextLevel(level);

        long currentXp = profile.getCurrentXp() != null ? profile.getCurrentXp() : 0L;

        while (currentXp >= xpNeeded) {
            currentXp -= xpNeeded;
            level++;

            Integer talentPoints = profile.getTalentPoints() != null ? profile.getTalentPoints() : 0;
            profile.setTalentPoints(talentPoints + 1);

            log.info("User {} leveled up to level {}", profile.getUser().getUsername(), level);
            notificationService.createNotification(profile.getUser(), com.lollito.fm.model.NotificationType.LEVEL_UP, "Level Up!", "You reached level " + level + "!", com.lollito.fm.model.NotificationPriority.HIGH);
            xpNeeded = xpForNextLevel(level);
        }

        profile.setLevel(level);
        profile.setCurrentXp(currentXp);
    }

    private long xpForNextLevel(int currentLevel) {
        // Simple formula: Level * 1000
        return currentLevel * LEVEL_XP_MULTIPLIER;
    }

    @Transactional
    public void unlockPerk(User user, ManagerPerk perk) {
        ManagerProfile profile = getProfile(user);

        if (hasPerk(user, perk)) {
            log.info("User {} already has perk {}", user.getUsername(), perk);
            return;
        }

        if (profile.getLevel() < perk.getRequiredLevel()) {
            throw new IllegalArgumentException("Manager level " + profile.getLevel() + " is not high enough for perk " + perk.getName() + " (requires " + perk.getRequiredLevel() + ")");
        }

        Integer talentPoints = profile.getTalentPoints() != null ? profile.getTalentPoints() : 0;

        if (talentPoints < 1) {
            throw new IllegalArgumentException("Not enough talent points to unlock perk " + perk.getName());
        }

        profile.setTalentPoints(talentPoints - 1);
        profile.getUnlockedPerks().add(perk);
        managerProfileRepository.save(profile);
        log.info("User {} unlocked perk {}", user.getUsername(), perk);
    }

    @Transactional(readOnly = true)
    public boolean hasPerk(User user, ManagerPerk perk) {
        return managerProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.getUnlockedPerks().contains(perk))
                .orElse(false);
    }
}
