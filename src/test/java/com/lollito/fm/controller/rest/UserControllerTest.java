package com.lollito.fm.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.mapper.UserMapper;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.LoginRequest;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    public void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("validUser");
        request.setPassword("validPassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("validUser");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            org.springframework.security.core.userdetails.User
                .withUsername("validUser")
                .password("validPassword")
                .authorities(new ArrayList<>())
                .build(),
            null
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.findByUsernameAndActive("validUser")).thenReturn(user);

        ResponseCookie cookie = ResponseCookie.from("fm_jwt", "token").path("/").maxAge(100).httpOnly(true).build();
        when(jwtUtils.generateJwtCookie(any(User.class))).thenReturn(cookie);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("fm_jwt", "token"))
                .andExpect(cookie().httpOnly("fm_jwt", true))
                .andExpect(cookie().path("fm_jwt", "/"))
                .andExpect(cookie().maxAge("fm_jwt", 100));
    }

    @Test
    public void testLoginInvalidUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("ab"); // Too short
        request.setPassword("validPassword");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("validUser");
        request.setPassword("12345"); // Too short

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogout() throws Exception {
        ResponseCookie cookie = ResponseCookie.from("fm_jwt", "").path("/").maxAge(0).build();
        when(jwtUtils.getCleanJwtCookie()).thenReturn(cookie);

        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("fm_jwt", ""))
                .andExpect(cookie().maxAge("fm_jwt", 0));
    }
}
