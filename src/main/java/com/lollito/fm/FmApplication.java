package com.lollito.fm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

//@EntityScan(
//        basePackageClasses = {FmApplication.class, Jsr310JpaConverters.class}
//)
@EnableAspectJAutoProxy
@SpringBootApplication
public class FmApplication {

	public static void main(String[] args) {
		SpringApplication.run(FmApplication.class, args);
	}
	
}
