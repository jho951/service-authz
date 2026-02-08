package com.example.enroll.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		Info info = new Info()
			.title("University Enroll API")
			.version("0.1.0")
			.description("Stable Spec 기반 수강신청 시스템 REST API 문서")
			.contact(new Contact()
				.name("Backend Team")
				.email("backend@example.com"));

		Server local = new Server()
			.url("/")
			.description("Local");

		return new OpenAPI()
			.info(info)
			.servers(List.of(local));
	}
}
