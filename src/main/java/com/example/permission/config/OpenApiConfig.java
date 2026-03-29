package com.example.permission.config;

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
			.title("Permission Service API")
			.version("0.1.0")
			.description("Gateway-admin 경로 인가 판정용 내부 API 문서")
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
