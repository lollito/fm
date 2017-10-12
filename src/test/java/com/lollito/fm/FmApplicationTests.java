package com.lollito.fm;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.test.context.junit4.SpringRunner;

import com.lollito.fm.bean.SessionBean;
import com.lollito.fm.model.Game;
import com.lollito.fm.service.ClubService;
import com.lollito.fm.service.FormationService;
import com.lollito.fm.service.GameService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class FmApplicationTests {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired GameService gameService;
	@Autowired SessionBean sessionBean;
	@Autowired FormationService formationService;
	
	@Test
	public void contextLoads() {
		logger.info("#####test#####");
		Game game = gameService.create("test", "test");
		sessionBean.setGameId(game.getId());
		LocalDate endDate = LocalDate.of( 2017 , Month.AUGUST , 26 );
//		Hibernate.initialize(game.getClubs());
		for (LocalDate date = game.getCurrentDate(); date.isBefore(endDate); date = date.plusDays(1)){
		    gameService.next(null);
		}
	}


}
