package com.lollito.fm.service;

import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.ManagerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerProgressionService {

    @Autowired
    private ManagerProfileRepository managerProfileRepository;

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
