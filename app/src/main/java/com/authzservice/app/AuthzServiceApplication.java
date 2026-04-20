package com.authzservice.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.authzservice")
public class AuthzServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthzServiceApplication.class, args);
	}
}
