package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.News;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.repository.rest.CountryRepository;
import com.lollito.fm.repository.rest.RoleRepository;
import com.lollito.fm.repository.rest.UserRepository;

@Service
public class UserService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired UserRepository userRepository;
	@Autowired ClubService clubService;
	@Autowired NewsService newsService;
	@Autowired RoleRepository roleRepository;
	@Autowired CountryRepository countryRepository;
	@Autowired BCryptPasswordEncoder bCryptPasswordEncoder;
	    
	public User save(RegistrationRequest request) {
		logger.info("request {}", request);
		if ( userRepository.existsByUsername(request.getUsername()) ) {
			throw new RuntimeException("Username alredy exist");
		}
		
		User user = new User(request);
		user.setCountry(countryRepository.findById(request.getCountryId()).get());
		user.setActive(true);
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setClub(clubService.findTopByLeagueCountryAndUserIsNull(user.getCountry()));
        //TODO temp roles
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        
        News news = new News(String.format("%s is the new coach of %s", user.getUsername(), user.getClub().getName()) , LocalDateTime.now());
        newsService.save(news);
        return userRepository.save(user);
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
	
	public User getLoggedUser ( ) {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	String username = null;
    	if (principal instanceof UserDetails) {
    		username =  ((UserDetails)principal).getUsername();
		} else {
			username =  principal.toString();
		}
    	return getUser( username );
    }
	
    public User getUser(String username) {
        return userRepository
          .findByUsername(username)
          .orElseThrow(() -> new RuntimeException("User '" + username + "' non trovato"));
    }
}
