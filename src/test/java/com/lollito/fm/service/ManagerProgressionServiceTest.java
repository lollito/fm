package com.lollito.fm.service;

import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.ManagerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerProgressionServiceTest {

    @Mock
    private ManagerProfileRepository managerProfileRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ManagerProgressionService managerProgressionService;

    private User user;
    private ManagerProfile profile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("manager1");

        profile = ManagerProfile.builder()
                .id(1L)
                .user(user)
                .level(1)
                .currentXp(0L)
                .talentPoints(0)
                .unlockedPerks(new HashSet<>())
                .build();
    }

    @Test
    void getProfile_ShouldCreateNewProfile_WhenNoneExists() {
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(managerProfileRepository.save(any(ManagerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ManagerProfile result = managerProgressionService.getProfile(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(1, result.getLevel());
        assertEquals(0L, result.getCurrentXp());
        assertEquals(0, result.getTalentPoints());
        assertNotNull(result.getUnlockedPerks());
        assertTrue(result.getUnlockedPerks().isEmpty());

        verify(managerProfileRepository).save(any(ManagerProfile.class));
    }

    @Test
    void getProfile_ShouldReturnExistingProfile_WhenExists() {
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        ManagerProfile result = managerProgressionService.getProfile(user);

        assertEquals(profile, result);
        verify(managerProfileRepository, never()).save(any(ManagerProfile.class));
    }

    @Test
    void addXp_ShouldIncrementXp_WhenNoLevelUp() {
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        managerProgressionService.addXp(user, 500L);

        assertEquals(500L, profile.getCurrentXp());
        assertEquals(1, profile.getLevel());
        assertEquals(0, profile.getTalentPoints());
        verify(managerProfileRepository).save(profile);
    }

    @Test
    void addXp_ShouldLevelUp_WhenThresholdReached() {
        // Threshold for level 1 is 1000
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        managerProgressionService.addXp(user, 1200L);

        // 1200 XP -> Level up -> 1200 - 1000 = 200 XP left
        // Level becomes 2
        // Talent points becomes 1

        assertEquals(200L, profile.getCurrentXp());
        assertEquals(2, profile.getLevel());
        assertEquals(1, profile.getTalentPoints());
        verify(managerProfileRepository).save(profile);
    }

    @Test
    void addXp_ShouldHandleMultipleLevelUps() {
        // Threshold for level 1: 1000
        // Threshold for level 2: 2000
        // Total needed for level 3: 3000
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        managerProgressionService.addXp(user, 3500L);

        // 3500 >= 1000 -> Level 2, XP 2500, TP 1
        // 2500 >= 2000 -> Level 3, XP 500, TP 2

        assertEquals(500L, profile.getCurrentXp());
        assertEquals(3, profile.getLevel());
        assertEquals(2, profile.getTalentPoints());
        verify(managerProfileRepository).save(profile);
    }

    @Test
    void unlockPerk_ShouldUnlockPerk_WhenRequirementsMet() {
        // Set up profile to meet requirements for a perk
        // e.g. VIDEO_ANALYST requires level 1
        profile.setLevel(1);
        profile.setTalentPoints(1);

        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        managerProgressionService.unlockPerk(user, ManagerPerk.VIDEO_ANALYST);

        assertTrue(profile.getUnlockedPerks().contains(ManagerPerk.VIDEO_ANALYST));
        assertEquals(0, profile.getTalentPoints());
        verify(managerProfileRepository).save(profile);
    }

    @Test
    void unlockPerk_ShouldThrowException_WhenLevelTooLow() {
        // MOTIVATOR requires level 5
        profile.setLevel(1);
        profile.setTalentPoints(10);

        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        assertThrows(IllegalArgumentException.class, () ->
            managerProgressionService.unlockPerk(user, ManagerPerk.MOTIVATOR)
        );

        verify(managerProfileRepository, never()).save(profile);
    }

    @Test
    void unlockPerk_ShouldThrowException_WhenNotEnoughPoints() {
        // VIDEO_ANALYST requires level 1
        profile.setLevel(1);
        profile.setTalentPoints(0);

        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        assertThrows(IllegalArgumentException.class, () ->
            managerProgressionService.unlockPerk(user, ManagerPerk.VIDEO_ANALYST)
        );

        verify(managerProfileRepository, never()).save(profile);
    }

    @Test
    void unlockPerk_ShouldDoNothing_WhenAlreadyUnlocked() {
        profile.setLevel(5);
        profile.setTalentPoints(10);
        profile.getUnlockedPerks().add(ManagerPerk.VIDEO_ANALYST);

        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        managerProgressionService.unlockPerk(user, ManagerPerk.VIDEO_ANALYST);

        // Should not deduct points
        assertEquals(10, profile.getTalentPoints());
        // Should not save
        verify(managerProfileRepository, never()).save(profile);
    }

    @Test
    void hasPerk_ShouldReturnTrue_WhenPerkIsUnlocked() {
        profile.getUnlockedPerks().add(ManagerPerk.VIDEO_ANALYST);
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        assertTrue(managerProgressionService.hasPerk(user, ManagerPerk.VIDEO_ANALYST));
    }

    @Test
    void hasPerk_ShouldReturnFalse_WhenPerkIsNotUnlocked() {
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        assertFalse(managerProgressionService.hasPerk(user, ManagerPerk.VIDEO_ANALYST));
    }

    @Test
    void hasPerk_ShouldReturnFalse_WhenProfileDoesNotExist() {
        when(managerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertFalse(managerProgressionService.hasPerk(user, ManagerPerk.VIDEO_ANALYST));
    }
}
