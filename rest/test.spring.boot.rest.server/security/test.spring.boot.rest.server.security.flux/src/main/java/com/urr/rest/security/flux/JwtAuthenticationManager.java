package com.urr.rest.security.flux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.urr.rest.security.JwtAuthenticatedProfile;
import com.urr.rest.security.JwtAuthenticationException;
import com.urr.rest.security.JwtTokenService;

import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationManager.class);

	@Autowired
	private JwtTokenService jwtTokenService;

	@Override
	public Mono<Authentication> authenticate(Authentication theAuthentication) {
		try {
			String aToken = (String) theAuthentication.getCredentials();
			String aUsername = jwtTokenService.getUsernameFromToken(aToken);

			return Mono.just(
					jwtTokenService.validateToken(aToken)
							.map(aBoolean -> new JwtAuthenticatedProfile(aUsername))
							.orElseThrow(() -> new JwtAuthenticationException("JWT Token validation failed")));

		}
		catch (JwtException ex) {
			log.error(String.format("Invalid JWT Token: %s", ex.getMessage()));
			throw new JwtAuthenticationException("Failed to verify token");
		}
	}

}
