package com.urr.rest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urr.rest.security.JwtTokenResponse;
import com.urr.rest.security.JwtTokenService;

@SpringBootApplication
@RestController
public class Application {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	private JwtTokenService jwtTokenService;

	@RequestMapping("/login")
	public ResponseEntity<JwtTokenResponse> login() {
		return new ResponseEntity<>(generateJwtToken("test"), HttpStatus.OK);
	}

	public JwtTokenResponse generateJwtToken(String username) {
		return new JwtTokenResponse(jwtTokenService.generateToken(username));
	}

	@RequestMapping("/api/msg")
	public Message printDateMessage() {
		logger.info("printDateMessage called with " + dummy());
		return new Message("Hello at " + new Date().toString());
	}

	@Bean
	protected String dummy() {
		return "dummy";
	}

	public static class Message {
		private String printDate;

		public Message(String thePrintDate) {
			printDate = thePrintDate;
		}

		public String getPrintDate() {
			return printDate;
		}
	}
}
