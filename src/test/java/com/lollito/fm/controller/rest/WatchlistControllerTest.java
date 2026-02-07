package com.lollito.fm.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.dto.AddToWatchlistRequest;
import com.lollito.fm.dto.WatchlistDTO;
import com.lollito.fm.model.WatchlistEntry;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;
import com.lollito.fm.service.WatchlistService;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.User;
import com.lollito.fm.model.Club;

@WebMvcTest(WatchlistController.class)
public class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchlistService watchlistService;

    @MockBean
    private UserService userService;

    // Security Mocks
    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    public void testGetClubWatchlist() throws Exception {
        Long clubId = 1L;

        User user = new User();
        Club club = new Club();
        club.setId(clubId);
        user.setClub(club);
        when(userService.getLoggedUser()).thenReturn(user);
        WatchlistDTO watchlistDTO = WatchlistDTO.builder().id(10L).build();

        when(watchlistService.getClubWatchlist(clubId)).thenReturn(watchlistDTO);

        mockMvc.perform(get("/api/watchlist/club/{clubId}", clubId))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testAddPlayerToWatchlist() throws Exception {
        Long clubId = 1L;
        Long playerId = 10L;

        User user = new User();
        Club club = new Club();
        club.setId(clubId);
        user.setClub(club);
        when(userService.getLoggedUser()).thenReturn(user);
        AddToWatchlistRequest request = new AddToWatchlistRequest();
        request.setNotes("Test Note");

        WatchlistEntry entry = new WatchlistEntry();
        entry.setId(100L);
        Player player = new Player();
        player.setId(playerId);
        player.setName("Test");
        player.setSurname("Player");
        player.setBirth(java.time.LocalDate.now().minusYears(20));
        entry.setPlayer(player);
        entry.setTotalNotifications(0);

        when(watchlistService.addPlayerToWatchlist(any(Long.class), any(Long.class), any(AddToWatchlistRequest.class)))
            .thenReturn(entry);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/watchlist/club/{clubId}/player/{playerId}", clubId, playerId)
               .with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content(json))
               .andExpect(status().isOk());
    }
}
