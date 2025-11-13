package com.mobi.mobi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

    @Value("${urls.frontend}")
    private String frontendUrl; // 이 변수는 이제 여기서는 사용되지 않지만, 다른 곳에서 쓸 수 있으니 둬도 됩니다.

    @Bean
    public OpenAPI openAPI() {
        final String jwtSchemeName = "bearerAuth";

        io.swagger.v3.oas.models.servers.Server apiServer =
                new io.swagger.v3.oas.models.servers.Server().url("https://api.mobi.ai.kr");

        return new OpenAPI()
                .servers(java.util.List.of(apiServer))
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


}
