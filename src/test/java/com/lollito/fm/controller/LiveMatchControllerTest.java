package com.lollito.fm.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.lollito.fm.config.security.WebSecurityConfig;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.service.LiveMatchService;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;

@WebMvcTest(controllers = LiveMatchController.class)
@Import(WebSecurityConfig.class)
public class LiveMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LiveMatchService liveMatchService;

    @MockBean
    private UserService userService; // Often required by security config

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean(name = "userDetailsServiceImpl")
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testFinishLiveMatchAsUser() throws Exception {
        mockMvc.perform(post("/api/live-match/1/finish").with(csrf()))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testFinishLiveMatchAsAdmin() throws Exception {
        doNothing().when(liveMatchService).forceFinish(1L);
        mockMvc.perform(post("/api/live-match/1/finish").with(csrf()))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testResetLiveMatchAsUser() throws Exception {
        mockMvc.perform(post("/api/live-match/1/reset").with(csrf()))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testResetLiveMatchAsAdmin() throws Exception {
        doNothing().when(liveMatchService).reset(1L);
        mockMvc.perform(post("/api/live-match/1/reset").with(csrf()))
               .andExpect(status().isOk());
    }
}
