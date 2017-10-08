package com.lollito.fm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EntityScan(
//        basePackageClasses = {FmApplication.class, Jsr310JpaConverters.class}
//)
@EnableAspectJAutoProxy
@EnableScheduling
@SpringBootApplication
public class FmApplication {

	public static void main(String[] args) {
		SpringApplication.run(FmApplication.class, args);
	}
	
}
