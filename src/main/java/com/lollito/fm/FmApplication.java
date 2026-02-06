package com.lollito.fm;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.lollito.fm.utils.NameGenerator;

//@EntityScan(
//        basePackageClasses = {FmApplication.class, Jsr310JpaConverters.class}
//)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync
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
