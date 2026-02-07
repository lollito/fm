package com.lollito.fm.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.mapper.ClubMapper;
import com.lollito.fm.mapper.SystemConfigurationMapper;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.User;
import com.lollito.fm.model.dto.ClubDTO;
import com.lollito.fm.model.dto.CreateClubRequest;
import com.lollito.fm.model.dto.UpdateClubRequest;
import com.lollito.fm.service.AdminService;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminController.class)
public class AdminControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UserService userService;

    @MockBean
    private ClubMapper clubMapper;

    @MockBean
    private SystemConfigurationMapper systemConfigurationMapper;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateClubWithInvalidData() throws Exception {
        CreateClubRequest request = new CreateClubRequest();
        // Leaving fields null/empty to trigger validation (once added)
        // e.g. name is null

        when(userService.getUser("admin")).thenReturn(new User());
        when(adminService.createClub(any(CreateClubRequest.class), any(User.class))).thenReturn(new Club());
        when(clubMapper.toDto(any(Club.class))).thenReturn(new ClubDTO());

        // Now expecting 400 Bad Request because validation is enabled.
        mockMvc.perform(post("/api/admin/clubs")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdateClubWithInvalidData() throws Exception {
        UpdateClubRequest request = new UpdateClubRequest();
        request.setInitialBudget(-100); // Invalid

        mockMvc.perform(put("/api/admin/clubs/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
    }
}
