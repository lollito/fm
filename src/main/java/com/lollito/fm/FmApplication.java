package com.lollito.fm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@EntityScan(
        basePackageClasses = {FmApplication.class, Jsr310JpaConverters.class}
)
@SpringBootApplication
public class FmApplication {

	public static void main(String[] args) {
		SpringApplication.run(FmApplication.class, args);
	}
}
