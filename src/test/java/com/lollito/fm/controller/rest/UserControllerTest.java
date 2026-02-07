package com.lollito.fm.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lollito.fm.config.security.jwt.AuthEntryPointJwt;
import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.service.UserDetailsServiceImpl;
import com.lollito.fm.service.UserService;
import com.lollito.fm.mapper.UserMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("user");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            new org.springframework.security.core.userdetails.User("user", "password", java.util.Collections.emptyList()),
            "password",
            java.util.Collections.emptyList()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.findByUsernameAndActive("user")).thenReturn(user);

        ResponseCookie cookie = ResponseCookie.from("fm_jwt", "token").path("/").maxAge(100).httpOnly(true).build();
        when(jwtUtils.generateJwtCookie(any(User.class))).thenReturn(cookie);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("fm_jwt", "token"))
                .andExpect(cookie().httpOnly("fm_jwt", true))
                .andExpect(cookie().path("fm_jwt", "/"))
                .andExpect(cookie().maxAge("fm_jwt", 100));
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
