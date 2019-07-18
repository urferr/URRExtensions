package com.urr.rest.security.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.urr.rest.security.JwtAuthenticatedProfile;
import com.urr.rest.security.JwtAuthenticationException;
import com.urr.rest.security.JwtTokenService;

import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationProvider.class);

	@Autowired
	private JwtTokenService jwtTokenService;

	@Override
	public Authentication authenticate(Authentication theAuthentication) throws AuthenticationException {
		try {
			String aToken = (String) theAuthentication.getCredentials();
			String aUsername = jwtTokenService.getUsernameFromToken(aToken);

			return jwtTokenService.validateToken(aToken)
					.map(aBoolean -> new JwtAuthenticatedProfile(aUsername))
					.orElseThrow(() -> new JwtAuthenticationException("JWT Token validation failed"));

		}
		catch (JwtException ex) {
			log.error(String.format("Invalid JWT Token: %s", ex.getMessage()));
			throw new JwtAuthenticationException("Failed to verify token");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return JwtAuthentication.class.equals(authentication);
	}
}