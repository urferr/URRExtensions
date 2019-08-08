package com.urr.rest.security.mvc;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(
			HttpServletRequest theRequest,
			HttpServletResponse theResponse,
			AuthenticationException theAuthException)
			throws IOException {
		theResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}