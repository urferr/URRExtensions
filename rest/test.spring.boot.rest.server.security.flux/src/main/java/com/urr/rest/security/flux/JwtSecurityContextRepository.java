package com.urr.rest.security.flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

	@Value("${jwt.header}")
	private String tokenHeader;

	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@Override
	public Mono<Void> save(ServerWebExchange theExchange, SecurityContext theContext) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Mono<SecurityContext> load(ServerWebExchange theExchange) {
		final String aRequestHeader = theExchange.getRequest().getHeaders().getFirst(this.tokenHeader);

		if (aRequestHeader != null && aRequestHeader.startsWith("Bearer ")) {
			String authToken = aRequestHeader.substring(7);
			Authentication aAuthenticationToken = new UsernamePasswordAuthenticationToken(authToken, authToken);
			return authenticationManager
					.authenticate(aAuthenticationToken).map((authentication) -> {
						return new SecurityContextImpl(authentication);
					});
		}

		return Mono.empty();
	}

}
