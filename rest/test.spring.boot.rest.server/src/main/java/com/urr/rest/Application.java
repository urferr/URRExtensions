package com.urr.rest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@RequestMapping("/api/string")
	public String printDate() {
		logger.info("printDate called");
		return "Hello at " + new Date().toString();
	}

	@RequestMapping("/api/msg")
	public Message printDateMessage() {
		logger.info("printDateMessage called");
		return new Message("Hello at " + new Date().toString());
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
