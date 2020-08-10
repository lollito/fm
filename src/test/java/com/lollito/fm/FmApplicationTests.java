package com.lollito.fm;

import java.time.LocalDate;
import java.time.Month;

import javax.transaction.Transactional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.lollito.fm.model.Game;
import com.lollito.fm.service.FormationService;
import com.lollito.fm.service.GameService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class FmApplicationTests {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired GameService gameService;
	@Autowired FormationService formationService;
	
	@Test
	@Ignore
	public void contextLoads() {
		Game game = gameService.create("test");
//		sessionBean.setGameId(game.getId());
		LocalDate endDate = LocalDate.of( 2018 , Month.AUGUST , 26 );
		formationService.createPlayerFormation();
		for (LocalDate date = game.getCurrentDate(); date.isBefore(endDate); date = date.plusDays(1)){
			logger.info("currdate {}", game.getCurrentDate());
		    gameService.next();
		}
//		for(Player player : game.getClubs().get(0).getTeam().getPlayers()) {
//			logger.info("player {}", player.getCondition());
//		}
	}


}
