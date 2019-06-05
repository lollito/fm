package com.lollito.fm;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.lollito.fm.utils.NameGenerator;

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
	
	@Bean
	public NameGenerator nameGenerator() throws IOException {
		return new NameGenerator("/name/custom.txt");	
	}
}
