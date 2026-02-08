package com.lollito.fm.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.UnlockPerkRequest;
import com.lollito.fm.service.ManagerProgressionService;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ManagerController.class)
public class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagerProgressionService managerProgressionService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User user;
    private ManagerProfile profile;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        profile = ManagerProfile.builder()
                .id(1L)
                .user(user)
                .level(2)
                .currentXp(500L)
                .talentPoints(3)
                .unlockedPerks(new HashSet<>())
                .build();
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testGetProfile() throws Exception {
        when(userService.getUser(any())).thenReturn(user);
        when(managerProgressionService.getProfile(any())).thenReturn(profile);

        mockMvc.perform(get("/api/manager/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.currentXp").value(500))
                .andExpect(jsonPath("$.talentPoints").value(3))
                .andExpect(jsonPath("$.xpForNextLevel").value(2000));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testUnlockPerk() throws Exception {
        UnlockPerkRequest request = new UnlockPerkRequest(ManagerPerk.VIDEO_ANALYST);

        Set<ManagerPerk> perks = new HashSet<>();
        perks.add(ManagerPerk.VIDEO_ANALYST);
        ManagerProfile updatedProfile = ManagerProfile.builder()
                .id(1L)
                .user(user)
                .level(2)
                .currentXp(500L)
                .talentPoints(2)
                .unlockedPerks(perks)
                .build();

        when(userService.getUser(any())).thenReturn(user);
        when(managerProgressionService.getProfile(any())).thenReturn(updatedProfile);

        mockMvc.perform(post("/api/manager/unlock-perk")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.talentPoints").value(2))
                .andExpect(jsonPath("$.unlockedPerks[0]").value("VIDEO_ANALYST"));
    }
}
