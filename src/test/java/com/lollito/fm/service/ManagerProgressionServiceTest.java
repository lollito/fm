package com.lollito.fm.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.ManagerProfileRepository;

@ExtendWith(MockitoExtension.class)
public class ManagerProgressionServiceTest {

    @Mock
    private ManagerProfileRepository managerProfileRepository;

    @InjectMocks
    private ManagerProgressionService managerProgressionService;

    @Test
    void testAddXp() {
        User user = new User();
        user.setId(1L);
        ManagerProfile profile = new ManagerProfile();
        profile.setCurrentXp(0L);
        user.setManagerProfile(profile);

        managerProgressionService.addXp(user, 100);

        assertEquals(100L, profile.getCurrentXp());
        verify(managerProfileRepository, times(1)).save(profile);
    }

    @Test
    void testAddXpNewProfile() {
        User user = new User();
        user.setId(1L);
        user.setManagerProfile(null);

        managerProgressionService.addXp(user, 100);

        verify(managerProfileRepository, times(1)).save(any(ManagerProfile.class));
    }
}
