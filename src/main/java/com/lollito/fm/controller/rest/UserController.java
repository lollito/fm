package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.config.security.jwt.JwtUtils;
import com.lollito.fm.mapper.UserMapper;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.JwtResponse;
import com.lollito.fm.model.rest.LoginRequest;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value="/api/user")
public class UserController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private UserService userService;
	
	@Autowired private AuthenticationManager authenticationManager;

	@Autowired private JwtUtils jwtUtils;

	@Autowired private UserMapper userMapper;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return userService.getCount();
    }
	
	@PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		logger.info("loginRequest {}", request);
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		User user = userService.findByUsernameAndActive(request.getUsername());

		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new JwtResponse(null,
												 user.getId(),
												 userDetails.getUsername(),
												 user.getClub() != null ? user.getClub().getId() : null,
												 roles));
    }

	@PostMapping("/logout")
	public ResponseEntity<?> logout() {
		ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body("You've been signed out!");
	}
	
	@PostMapping("/register")
    public ResponseEntity<?> registration(@RequestBody RegistrationRequest request) {

        User user = userService.save(request);

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new JwtResponse(null,
												 user.getId(),
												 userDetails.getUsername(),
												 user.getClub() != null ? user.getClub().getId() : null,
												 roles));
    }
    
    @GetMapping("/")
	public ResponseEntity<?> findAll (  ) {
		return ResponseEntity.ok(
            java.util.stream.StreamSupport.stream(userService.findAll().spliterator(), false)
                .map(userMapper::toDto)
                .collect(Collectors.toList())
        );
	}
}
