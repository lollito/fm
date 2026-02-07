package com.lollito.fm.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.service.DebugToolsService;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;

@WebMvcTest(controllers = DebugToolsController.class)
public class DebugToolsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DebugToolsService debugToolsService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetPlayer() throws Exception {
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        player.setName("Test");
        player.setSurname("Player");
        player.setStamina(80.0);

        when(debugToolsService.getPlayer(1L)).thenReturn(player);

        mockMvc.perform(get("/api/admin/debug/players/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Test"))
               .andExpect(jsonPath("$.surname").value("Player"))
               .andExpect(jsonPath("$.stamina").value(80.0));
    }
}
