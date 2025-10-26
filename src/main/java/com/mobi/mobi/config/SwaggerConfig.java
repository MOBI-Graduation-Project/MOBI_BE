package com.mobi.mobi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Profile 어노테이션이 있다면 삭제하여 모든 환경에서 동작하도록 합니다.
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    // application.yml 에 설정된 프론트엔드 URL을 주입받습니다.
    @Value("${urls.frontend}")
    private String frontendUrl;

    @Bean
    public OpenAPI openAPI() {
        final String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Mobi API Docs")
                        .version("v1")
                        .description("스웨거 문서 설명 적는 부분")
                )
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName,
                                new SecurityScheme()
                                        .name(jwtSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    // WebMvcConfigurer의 addCorsMappings를 오버라이드하여 CORS 설정을 관리합니다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 정책 적용
                // 환경 변수에서 읽어온 프론트엔드 URL을 허용합니다.
                .allowedOrigins(frontendUrl, "http://localhost:3000") // 로컬 개발용 포트도 추가 가능
                .allowedMethods("*") // 필요한 HTTP 메서드 명시
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 인증 정보 포함 허용
    }
}
