package com.urr.rest.security.flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@Autowired
	private ServerSecurityContextRepository securityContextRepository;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity theHttpSecurity) {
		theHttpSecurity
				.cors().and()
				.csrf().disable()
				.exceptionHandling().authenticationEntryPoint((theExchange, theException) -> {
					return Mono.fromRunnable(() -> {
						theExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					});
				})
				.and()
				.authenticationManager(authenticationManager)
				.securityContextRepository(securityContextRepository)
				.authorizeExchange()
				.pathMatchers("/login").permitAll()
				.anyExchange().authenticated();

		return theHttpSecurity.build();
	}
}
