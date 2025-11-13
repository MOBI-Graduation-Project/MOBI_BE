package com.mobi.mobi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
		servers = {
				@Server(url = "http://localhost:8080", description = "Local Development Server"),
				@Server(url = "https://api.mobi.ai.kr", description = "Mobi API Server (HTTPS)")
		}
)
@EnableJpaAuditing
@SpringBootApplication
public class MobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobiApplication.class, args);
	}
}
