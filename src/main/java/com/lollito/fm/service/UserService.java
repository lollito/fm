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
	
	public User create() {
		User user = new User();
		user.setName("stocazzo");
		userRepository.save(user);
		return user;
	}
	
	public User find(){
		return userRepository.findAll().iterator().next();
	}

	public Long getCount() {
		return userRepository.count();
	}
}
