package com.lollito.fm.controller.rest;

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

import com.lollito.fm.service.ClubService;
import com.lollito.fm.service.UserService;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.mapper.ClubMapper;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.dto.ClubDTO;

@WebMvcTest(controllers = ClubController.class)
public class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClubService clubService;

    @MockBean
    private UserService userService;

    @MockBean
    private CountryRepository countryRepository;

    @MockBean
    private ClubMapper clubMapper;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "lollito")
    public void testGetClubById() throws Exception {
        Club club = new Club();
        club.setId(1L);
        club.setName("Test Club");

        ClubDTO clubDTO = new ClubDTO();
        clubDTO.setId(1L);
        clubDTO.setName("Test Club");

        when(clubService.findById(1L)).thenReturn(club);
        when(clubMapper.toDto(club)).thenReturn(clubDTO);

        mockMvc.perform(get("/api/club/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Test Club"));
    }
}
