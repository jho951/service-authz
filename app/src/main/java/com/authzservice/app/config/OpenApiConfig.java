package com.authzservice.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod & !production & !live")
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		Info info = new Info()
			.title("Authz Service API")
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
			.components(new Components()
				.addSecuritySchemes("internalJwt",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Gateway-signed internal service JWT"))
				.addSecuritySchemes("internalSecret",
					new SecurityScheme()
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)
						.name("X-Internal-Request-Secret")
						.description("Legacy Gateway-to-Authz shared secret")))
			.servers(List.of(local));
	}

	@Bean
	public GroupedOpenApi internalPermissionApi() {
		return GroupedOpenApi.builder()
			.group("Internal Permission APIs")
			.pathsToMatch("/permissions/internal/**")
			.build();
	}

	@Bean
	public GroupedOpenApi healthApi() {
		return GroupedOpenApi.builder()
			.group("Health APIs")
			.pathsToMatch("/health", "/ready")
			.build();
	}
}
