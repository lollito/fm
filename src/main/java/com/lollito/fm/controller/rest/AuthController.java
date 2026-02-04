package com.lollito.fm.controller.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.config.security.JwtUtil;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.ApiResponse;
import com.lollito.fm.model.rest.LoginRequest;
import com.lollito.fm.model.rest.SignUpRequest;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired UserService userService;

    @Autowired CountryRepository countryRepository;

//    @Autowired
//    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());

        String jwt = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new ApiResponse(true, jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userService.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userService.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        
   
        // Creating user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getName(), signUpRequest.getSurname(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        Country country = countryRepository.findById(signUpRequest.getCountryId()).orElseThrow(() -> new RuntimeException("Invalid Country"));
        user.setCountry(country);
//        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
//                .orElseThrow(() -> new AppException("User Role not set."));

//        user.setRoles(Collections.singleton(userRole));

        User result = userService.create(user);

        

        return ResponseEntity.ok(new ApiResponse(true, result.getUsername()));
    }
}
