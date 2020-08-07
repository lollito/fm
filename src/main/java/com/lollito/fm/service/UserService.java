package com.lollito.fm.service;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.RoleRepository;
import com.lollito.fm.repository.rest.UserRepository;

@Service
public class UserService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired UserRepository userRepository;
	@Autowired ClubService clubService;
	@Autowired RoleRepository roleRepository;
	@Autowired BCryptPasswordEncoder bCryptPasswordEncoder;
	    
	public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }
    
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
}
