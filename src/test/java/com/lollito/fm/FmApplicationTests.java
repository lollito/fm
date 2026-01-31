package com.lollito.fm;

import java.time.LocalDateTime;
import java.time.Month;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.lollito.fm.model.Game;
import com.lollito.fm.service.FormationService;
import com.lollito.fm.service.GameService;

@SpringBootTest
@Transactional
@WithMockUser(username = "lollito")
public class FmApplicationTests {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired GameService gameService;
	@Autowired FormationService formationService;
	
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
