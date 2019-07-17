package com.urr.rest.security;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtTokenService {
	@Value("${jwt.secret}")
	private String secret;

	public String getUsernameFromToken(String theToken) {
		return getClaimFromToken(theToken, Claims::getSubject);
	}

	public Date getExpirationDateFromToken(String theToken) {
		return getClaimFromToken(theToken, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String theToken, Function<Claims, T> theClaimsResolver) {
		final Claims allClaims = getAllClaimsFromToken(theToken);
		return theClaimsResolver.apply(allClaims);
	}

	private Claims getAllClaimsFromToken(String theToken) {
		return Jwts.parser()
				.setSigningKey(secret.getBytes())
				.parseClaimsJws(theToken)
				.getBody();
	}

	private Boolean isTokenNotExpired(String theToken) {
		final Date expiration = getExpirationDateFromToken(theToken);
		return expiration.after(new Date());
	}

	public Optional<Boolean> validateToken(String theToken) {
		return isTokenNotExpired(theToken) ? Optional.of(Boolean.TRUE) : Optional.empty();
	}
}
