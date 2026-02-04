package com.lollito.fm.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        
		@Autowired
		private JwtUtil jwtUtil;
		
		@Autowired
		private UserDetailsServiceImpl userDetailsService;

		@Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
		
		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		}

		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}

		@Override
        protected void configure(HttpSecurity http) throws Exception {
            http
            .csrf().disable()
            	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            	.addFilterAfter(new JwtTokenAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class )
            	.authorizeRequests()
			.antMatchers("/api/auth/**").permitAll()
			.antMatchers("/api/**").authenticated()
			.antMatchers("/actuator/**").authenticated()
	                .anyRequest().permitAll()
            .and()
            .httpBasic();
        }
}
