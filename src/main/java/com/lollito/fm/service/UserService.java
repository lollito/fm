package com.lollito.fm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.UserRepository;

@Service
public class UserService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired UserRepository userRepository;
	@Autowired ClubService clubService;
	
	public User create(User user) {
		user.setClub(clubService.findTopByLeagueCountryAndUserIsNull(user.getCountry()));
		userRepository.save(user);
		return user;
	}
	
	public User find(){
		return userRepository.findAll().iterator().next();
	}

	public Long getCount() {
		return userRepository.count();
	}

	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public User findByUsernameOrEmail(String usernameOrEmail) {
		return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}
}
