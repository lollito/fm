package com.lollito.fm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lollito.fm.repository.ManagerProfileRepository;
import com.lollito.fm.repository.rest.UserRepository;

@SpringBootTest
@Transactional
public class ManagerProfileTest {

    @Autowired
    private ManagerProfileRepository managerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testManagerProfilePersistence() {
        // Create User
        User user = User.builder()
                .username("testmanager")
                .email("manager@test.com")
                .password("password")
                .build();
        user = userRepository.save(user);

        // Create ManagerProfile
        ManagerProfile profile = ManagerProfile.builder()
                .user(user)
                .level(1)
                .currentXp(0L)
                .talentPoints(0)
                .build();

        // Save Profile
        profile = managerProfileRepository.save(profile);

        // Verify Persistence
        assertNotNull(profile.getId());
        assertEquals(user.getId(), profile.getUser().getId());

        // Verify Find By User Id
        Optional<ManagerProfile> foundProfile = managerProfileRepository.findByUserId(user.getId());
        assertTrue(foundProfile.isPresent());
        assertEquals(profile.getId(), foundProfile.get().getId());

        // Test Perks
        profile.getUnlockedPerks().add(ManagerPerk.VIDEO_ANALYST);
        profile = managerProfileRepository.save(profile);

        ManagerProfile updatedProfile = managerProfileRepository.findById(profile.getId()).orElseThrow();
        assertTrue(updatedProfile.getUnlockedPerks().contains(ManagerPerk.VIDEO_ANALYST));
        assertEquals(1, updatedProfile.getUnlockedPerks().size());
    }
}
