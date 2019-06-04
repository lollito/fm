package com.lollito.fm.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        
		@Autowired
		private JwtUtil jwtUtil;
		
		@Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
		
		@Override
        protected void configure(HttpSecurity http) throws Exception {
            http
            .csrf().disable()
            	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            	.addFilterAfter(new JwtTokenAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class )
            	.authorizeRequests()
            	//TODO check permit
//            		.antMatchers("/apiWeb/**").authenticated()
//            		.antMatchers("/actuator/**").authenticated()
	            	//.antMatchers("/auth/**","/api/**","/swagger-resources/**","/webjars/**","/v2/**","/swagger-ui.html").permitAll()
	                .anyRequest().permitAll()
            .and()
            .httpBasic();
        }
}
