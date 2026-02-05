package com.lollito.fm;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.lollito.fm.model.Game;
import com.lollito.fm.model.User;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.service.CountryService;
import com.lollito.fm.service.FormationService;
import com.lollito.fm.service.GameService;
import com.lollito.fm.service.ModuleService;
import com.lollito.fm.service.UserService;
import com.lollito.fm.repository.rest.UserRepository;

@SpringBootTest
@Transactional
public class FmApplicationTests {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired GameService gameService;
	@Autowired FormationService formationService;
	@Autowired UserService userService;
	@Autowired UserRepository userRepository;
	@Autowired CountryService countryService;
	@Autowired ModuleService moduleService;

	@BeforeEach
	public void setup() {
		UserDetails userDetails = new org.springframework.security.core.userdetails.User("lollito", "password", Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
		);
	}
	
	@Test
	public void contextLoads() {
		Game game = gameService.create("test");
//		sessionBean.setGameId(game.getId());
		LocalDateTime endDate = LocalDateTime.of( 2021 , Month.AUGUST , 26, 00, 00 );
		formationService.createPlayerFormation();
		for (LocalDateTime date = game.getCurrentDate(); date.isBefore(endDate); date = date.plusDays(1)){
			logger.info("currdate {}", game.getCurrentDate());
		    gameService.next();
		}
//		for(Player player : game.getClubs().get(0).getTeam().getPlayers()) {
//			logger.info("player {}", player.getCondition());
//		}
	}


}
