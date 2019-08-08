package com.urr.rest;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	//	@PostMapping("/api/msg")
	//	public ReturnMessage printDateMessage() {
	//		return new ReturnMessage("Hello' at " + new Date().toString());
	//	}

	//	@PostMapping("/api/msg")
	//	public ReturnMessage printDateMessage(@RequestParam("name") String theRequest) {
	//		return new ReturnMessage("Hello '" + theRequest + "' at " + new Date().toString());
	//	}

	@PostMapping("/api/msg")
	public ReturnMessage printDateMessage(@RequestBody RequestMessage theRequest) {
		return new ReturnMessage(theRequest.p1.getName() + " - " + theRequest.p2.getName() + "(" + theRequest.p2.getObj().getAttr() + ")" + " - " + theRequest.p3.getTimestamp());
	}

	public static class RequestMessage {
		public P1 p1;
		public P2 p2;
		public P3 p3;
	}

	public static class P1 implements Serializable {
		private String name;

		public P1() {
		}

		public P1(String theName) {
			name = theName;
		}

		public String getName() {
			return name;
		}
	}

	public static class P2 implements Serializable {
		private String name;
		private Obj obj;

		public P2() {
		}

		public P2(String theName, Obj theObj) {
			name = theName;
			obj = theObj;
		}

		public String getName() {
			return name;
		}

		public Obj getObj() {
			return obj;
		}

		public static class Obj implements Serializable {
			private String attr;

			public Obj() {
			}

			public Obj(String theAttr) {
				attr = theAttr;
			}

			public String getAttr() {
				return attr;
			}

		}
	}

	public static class P3 implements Serializable {
		private LocalDateTime timestamp;

		public P3() {
		}

		public P3(LocalDateTime theTimestamp) {
			timestamp = theTimestamp;
		}

		public LocalDateTime getTimestamp() {
			return timestamp;
		}

	}

	public static class ReturnMessage {
		private String answer;

		public ReturnMessage(String theAnswer) {
			answer = theAnswer;
		}

		public String getAnswer() {
			return answer;
		}
	}
}
