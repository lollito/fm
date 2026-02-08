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



    

   
import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;

import java.util.HashSet;
import com.lollito.fm.repository.ManagerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.ManagerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerProgressionService {

    private final ManagerProfileRepository managerProfileRepository;
private final Logger logger = LoggerFactory.getLogger(this.getClass());
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
                .build();
        return managerProfileRepository.save(profile);
                .orElseGet(() -> {
                    ManagerProfile profile = ManagerProfile.builder()
                            .user(user)
                            .level(1)
                            .currentXp(0L)
                            .talentPoints(0)
                            .unlockedPerks(new HashSet<>())
                            .build();
                    return managerProfileRepository.save(profile);
                });
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
        profile.setCurrentXp(profile.getCurrentXp() + amount);

        long xpForNextLevel = profile.getLevel() * LEVEL_XP_MULTIPLIER;
        while (profile.getCurrentXp() >= xpForNextLevel) {
            profile.setCurrentXp(profile.getCurrentXp() - xpForNextLevel);
            profile.setLevel(profile.getLevel() + 1);
            profile.setTalentPoints(profile.getTalentPoints() + 1);
            log.info("Manager {} leveled up to level {}", user.getUsername(), profile.getLevel());

            xpForNextLevel = profile.getLevel() * LEVEL_XP_MULTIPLIER;
        }

        managerProfileRepository.save(profile);
    }

    @Transactional
    public void unlockPerk(User user, ManagerPerk perk) {
        ManagerProfile profile = getProfile(user);

        if (profile.getUnlockedPerks().contains(perk)) {
            log.info("Manager {} already has perk {}", user.getUsername(), perk.getName());
            return;
        }

        if (profile.getLevel() < perk.getRequiredLevel()) {
            throw new IllegalArgumentException("Manager level " + profile.getLevel() + " is not high enough for perk " + perk.getName() + " (requires " + perk.getRequiredLevel() + ")");
        }

        if (profile.getTalentPoints() < 1) {
            throw new IllegalArgumentException("Not enough talent points to unlock perk " + perk.getName());
        }

        profile.setTalentPoints(profile.getTalentPoints() - 1);
        profile.getUnlockedPerks().add(perk);
        managerProfileRepository.save(profile);
        log.info("Manager {} unlocked perk {}", user.getUsername(), perk.getName());
    }

    @Transactional(readOnly = true)
    public boolean hasPerk(User user, ManagerPerk perk) {
        return managerProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.getUnlockedPerks().contains(perk))
                .orElse(false);

    }
   
    @Transactional
    public void addXp(User user, Integer xp) {
        if (user == null || xp == null || xp <= 0) {
            return;
        }

        ManagerProfile profile = user.getManagerProfile();
        if (profile == null) {
            profile = new ManagerProfile();
            profile.setUser(user);
            user.setManagerProfile(profile);
            // Since ManagerProfile is the owning side (no mappedBy), saving it should work.
        }

        Long currentXp = profile.getCurrentXp();
        profile.setCurrentXp((currentXp == null ? 0L : currentXp) + xp);

        // Simple level up logic stub (can be expanded later)
        // For now just add XP.

        managerProfileRepository.save(profile);
    }
}
