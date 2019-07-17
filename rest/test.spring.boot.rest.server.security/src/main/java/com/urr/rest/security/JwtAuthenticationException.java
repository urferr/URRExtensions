package com.urr.rest.security;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String theMessage) {
        super(theMessage);
    }
}
