package com.lollito.fm.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.User;
import com.lollito.fm.service.SecurityService;
import com.lollito.fm.service.UserService;

@RestController
@RequestMapping(value="/api/user")
public class UserController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private UserService userService;
	
	@Autowired private SecurityService securityService;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return userService.getCount();
    }
	
	@PostMapping("/register")
    public ResponseEntity<?> registration(@RequestBody User user) {

        userService.save(user);

        securityService.autoLogin(user.getUsername(), user.getPasswordConfirm());

        return ResponseEntity.ok(user );
    }
    
    @GetMapping("/")
	public ResponseEntity<?> findAll (  ) {
		return ResponseEntity.ok( userService.findAll() );
	}
}
