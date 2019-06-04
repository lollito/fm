package com.lollito.fm.controller.rest;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.config.security.JwtUtil;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.ApiResponse;
import com.lollito.fm.model.rest.SignUpRequest;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.repository.rest.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

//    @Autowired
//    AuthenticationManager authenticationManager;

    @Autowired UserRepository userRepository;

    @Autowired CountryRepository countryRepository;

//    @Autowired
//    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

//    @PostMapping("/signin")
//    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUsernameOrEmail(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String jwt = jwtUtil.generateToken(authentication);
//        return ResponseEntity.ok(new ApiResponse(true, jwt));
//    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
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

        User result = userRepository.save(user);

        

        return ResponseEntity.ok(new ApiResponse(true, result.getUsername()));
    }
}
