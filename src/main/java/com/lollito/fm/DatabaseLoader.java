package com.lollito.fm;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.lollito.fm.model.AdminRole;
import com.lollito.fm.model.Role;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.repository.rest.RoleRepository;
import com.lollito.fm.repository.rest.UserRepository;
import com.lollito.fm.service.CountryService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.lollito.fm.service.ServerService;
import com.lollito.fm.service.ModuleService;
import com.lollito.fm.service.UserService;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	
	@Autowired private ModuleService moduleService;
	@Autowired private ServerService serverService;
	@Autowired private CountryService countryService;
	@Autowired private UserService userService;
	@Autowired private UserRepository userRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("Loading Database");

		// Initialize Roles
		Role roleUser = roleRepository.findByName("ROLE_USER");
		if (roleUser == null) {
			roleUser = roleRepository.save(Role.builder().name("ROLE_USER").build());
		}

		Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
		if (roleAdmin == null) {
			roleAdmin = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
		}

		// Setup System User for Server Creation
		if (!userRepository.existsByUsername("system")) {
			User systemUser = new User();
			systemUser.setUsername("system");
			systemUser.setPassword("syspass");
			systemUser.setEmail("sys@sys.com");
			systemUser.setActive(true);
			systemUser.setAdminRole(AdminRole.SUPER_ADMIN);
			systemUser.getRoles().add(roleAdmin);
			userRepository.save(systemUser);
		} else {
			User systemUser = userRepository.findByUsernameAndActive("system", true);
			if (systemUser != null) {
				boolean changed = false;
				if (systemUser.getAdminRole() == null) {
					systemUser.setAdminRole(AdminRole.SUPER_ADMIN);
					changed = true;
				}
				if (!systemUser.getRoles().contains(roleAdmin)) {
					systemUser.getRoles().add(roleAdmin);
					changed = true;
				}
				if (changed) {
					userRepository.save(systemUser);
				}
			}
		}

		// Setup Admin User
		if (!userRepository.existsByUsername("admin")) {
			User adminUser = new User();
			adminUser.setUsername("admin");
			adminUser.setPassword(bCryptPasswordEncoder.encode("admin"));
			adminUser.setEmail("admin@admin.com");
			adminUser.setActive(true);
			adminUser.setAdminRole(AdminRole.SUPER_ADMIN);
			adminUser.getRoles().add(roleAdmin);
			userRepository.save(adminUser);
		}

		// Auth as system to create game (which creates clubs)
		UserDetails sysDetails = new org.springframework.security.core.userdetails.User("system", "syspass", Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(sysDetails, null, sysDetails.getAuthorities())
		);

		if (moduleService.findAll().isEmpty()) {
			moduleService.createModules();
		}
		if (countryService.getCount() == 0) {
			countryService.create();
		}

		if(serverService.findAll().isEmpty()) {
			serverService.create("Alpha");
		}

		// Create default user
		if (!userService.existsByUsername("lollito")) {
			RegistrationRequest registration = new RegistrationRequest();
			registration.setEmail("ciao");
			registration.setPassword("ciao");
			registration.setPasswordConfirm("ciao");
			registration.setUsername("lollito");
			registration.setClubName("Roma");
			registration.setCountryId(countryService.findByCreateLeague(true).get(0).getId());
			userService.save(registration);
		}

		logger.info("Database Loaded");
	}

}
