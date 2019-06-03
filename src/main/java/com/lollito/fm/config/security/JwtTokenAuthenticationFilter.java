package com.lollito.fm.config.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtUtil jwtUtil;
	
	public JwtTokenAuthenticationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String header = request.getHeader(jwtUtil.getHeader());
		if(header == null || !header.startsWith(jwtUtil.getPrefix())) {
			filterChain.doFilter(request, response);
			return;
			
		}
		
		String token = header.replace(jwtUtil.getPrefix(), "");

		try {	
			
			Claims claims = jwtUtil.parseToken(token);
			
			String username = claims.getSubject();
			if(username != null) {
				
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, null);
				UserObject userObject = new UserObject(username, String.valueOf( claims.get("userId") ) );
				auth.setDetails( userObject );
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
			if ( e instanceof ExpiredJwtException ) {
				throw new TokenExpiredException ("Token Expired");
			}
			throw e;
		}
		
		filterChain.doFilter(request, response);
	}

}
