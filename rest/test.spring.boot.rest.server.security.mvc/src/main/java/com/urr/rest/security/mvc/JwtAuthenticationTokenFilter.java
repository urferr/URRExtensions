package com.urr.rest.security.mvc;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

	@Value("${jwt.header}")
	private String tokenHeader;

	@Override
	protected void doFilterInternal(HttpServletRequest theRequest, HttpServletResponse theResponse, FilterChain theFilterChain) throws ServletException, IOException {
		final String aRequestHeader = theRequest.getHeader(this.tokenHeader);

		if (aRequestHeader != null && aRequestHeader.startsWith("Bearer ")) {
			String authToken = aRequestHeader.substring(7);
			JwtAuthentication authentication = new JwtAuthentication(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		theFilterChain.doFilter(theRequest, theResponse);
	}
}