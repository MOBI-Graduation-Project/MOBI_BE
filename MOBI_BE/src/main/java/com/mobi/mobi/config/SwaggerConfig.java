package com.mobi.mobi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    @Value("${urls.frontend}")
    private String frontendUrl;   // 예: https://mobi.ai.kr

    @Value("${urls.backend}")
    private String backendUrl;    // 예: https://api.mobi.ai.kr

    @Bean
    public OpenAPI openAPI() {
        final String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .addServersItem(new Server()
                        .url(backendUrl) // HTTPS로 고정
                        .description("Mobi API Server (HTTPS)"))
                .info(new Info()
                        .title("Mobi API Docs")
                        .version("v1")
                        .description("모비 API 명세서입니다."))
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName,
                                new SecurityScheme()
                                        .name(jwtSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        frontendUrl,
                        "https://mobi.ai.kr",
                        "https://www.mobi.ai.kr",
                        "https://api.mobi.ai.kr",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 프리플라이트 캐시(초)
    }
}
