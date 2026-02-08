package com.lollito.fm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.ManagerProfileRepository;

@Service
public class ManagerProgressionService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ManagerProfileRepository managerProfileRepository;

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
                .build();
        return managerProfileRepository.save(profile);
    }

    @Transactional
    public void addXp(User user, long amount) {
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

            logger.info("User {} leveled up to level {}", profile.getUser().getUsername(), level);
            xpNeeded = xpForNextLevel(level);
        }

        profile.setLevel(level);
        profile.setCurrentXp(currentXp);
    }

    private long xpForNextLevel(int currentLevel) {
        // Simple formula: Level * 1000
        return currentLevel * 1000L;
    }

    @Transactional
    public void unlockPerk(User user, ManagerPerk perk) {
        ManagerProfile profile = getProfile(user);
        if (hasPerk(user, perk)) {
            logger.info("User {} already has perk {}", user.getUsername(), perk);
            return;
        }

        // Basic validation logic
        Integer talentPoints = profile.getTalentPoints() != null ? profile.getTalentPoints() : 0;

        if (talentPoints > 0) {
            profile.setTalentPoints(talentPoints - 1);
            profile.getUnlockedPerks().add(perk);
            managerProfileRepository.save(profile);
            logger.info("User {} unlocked perk {}", user.getUsername(), perk);
        } else {
            logger.warn("User {} does not have enough talent points to unlock {}", user.getUsername(), perk);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasPerk(User user, ManagerPerk perk) {
        ManagerProfile profile = getProfile(user);
        return profile.getUnlockedPerks().contains(perk);
    }
}
